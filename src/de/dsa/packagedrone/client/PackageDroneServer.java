/************************************************************************
 *                                                                      *
 *  DDDD     SSSS    AAA        Daten- und Systemtechnik Aachen GmbH    *
 *  D   D   SS      A   A       Pascalstrasse 28                        *
 *  D   D    SSS    AAAAA       52076 Aachen-Oberforstbach, Germany     *
 *  D   D      SS   A   A       Telefon: +49 (0)2408 / 9492-0           *
 *  DDDD    SSSS    A   A       Telefax: +49 (0)2408 / 9492-92          *
 *                                                                      *
 *                                                                      *
 *  (c) Copyright by DSA - all rights reserved                          *
 *                                                                      *
 ************************************************************************
 *
 * Initial Creation:
 *    Author      sro
 *    Created on  23 Dec 2016
 *
 ************************************************************************/
package de.dsa.packagedrone.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.dsa.packagedrone.client.model.Artifact;
import de.dsa.packagedrone.client.model.Channel;
import de.dsa.packagedrone.client.util.ConnectionUtil;
import de.dsa.packagedrone.client.util.FilterUtil;
import de.dsa.packagedrone.client.util.HttpResponseValidator;
import de.dsa.packagedrone.client.util.ValidationUtil;

public class PackageDroneServer {

    private static final String dot = "\\.";

    public String getHost() {
        return host;
    }

    private String host;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
    private Map<String, String> cookies;

    public Map<String, String> getCookies() {
        return cookies;
    }

    public PackageDroneServer(String host, String username, String password) throws IOException {
        this.host = host;
        this.cookies = login(username, password);
    }

    public Map<String, String> login(String username, String password) throws IOException {
        // # Go to login page
        Connection.Response loginFormResponse = Jsoup.connect(host + "/login").method(Connection.Method.GET).userAgent(USER_AGENT).execute();

        // # Fill the login form
        // ## Find the form first...
        FormElement loginForm = (FormElement)loginFormResponse.parse().select("#command").first();
        ValidationUtil.checkElement("Login Form", loginForm);

        // ## ... then "type" the username ...
        Element loginField = loginForm.select("#email").first();
        ValidationUtil.checkElement("Login Field", loginField);
        loginField.val(username);

        // ## ... and "type" the password
        Element passwordField = loginForm.select("#password").first();
        ValidationUtil.checkElement("Password Field", passwordField);
        passwordField.val(password);

        // # Now send the form for login
        Connection.Response loginActionResponse = loginForm.submit().cookies(loginFormResponse.cookies()).userAgent(USER_AGENT).execute();

        return loginFormResponse.cookies();
        //        return loginForm.submit().cookies(loginFormResponse.cookies()).userAgent(USER_AGENT).execute().cookies();
    }

    public List<Channel> parseChannels(String jsonString) throws IOException {
        List<Channel> channels = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
        JsonArray jsonMainArr = jsonObject.getAsJsonArray("channels");
        JsonParser jsonParser = new JsonParser();
        for (JsonElement jsonElement : jsonMainArr) {
            JsonElement element = jsonParser.parse(jsonElement.getAsJsonObject().toString());
            Channel channel = gson.fromJson(element, Channel.class);
            channel.addArtifacts(getArtifacts(channel.getId()));
            channels.add(channel);
        }
        return channels;
    }

    public List<Channel> getChannels() throws Exception {
        String jsonString = ConnectionUtil.readUrl(host + "/api/channels");
        return parseChannels(jsonString);
    }

    public List<Artifact> getArtifacts(String channelId) throws IOException {
        Document doc = getDocument(this.host + "/channel/" + channelId + "/view");
        List<Artifact> artifacts = new ArrayList<>();
        Element table = doc.select("table").get(0); //select the first table.
        Elements rows = table.select("tr");
        rows.remove(0);//first row is the col names so skip it.
        //        rows.parallelStream().forEach((row) -> {
        rows.stream().forEach((row) -> {
            int dataLevel = Integer.parseInt(row.attr("data-level"));
            if (dataLevel == 0) {
                Elements cols = row.select("td");
                String dataArtifactId = cols.get(6).select("a").attr("data-artifact-id");
                if (!StringUtil.isBlank(dataArtifactId)) {// We only consider real files which can be deleted.
                    try {
                        Artifact artifact = parseArtifactDetails(channelId, dataArtifactId);
                        if (artifact != null) {
                            artifacts.add(artifact);
                        }
                    } catch (IOException e) {
                        System.err.println(channelId + "|" + dataArtifactId + "can not be parsed!");
                        e.printStackTrace();
                    }
                }
            }
        });
        return artifacts;
    }

    public Artifact parseArtifactDetails(String channelId, String dataArtifactId) throws IOException {
        Artifact artifact = new Artifact();
        Map<String, String> metadata = new HashMap<>();
        Document doc = getDocument(this.host + "/channel/" + channelId + "/artifacts/" + dataArtifactId + "/view");
        Element table = doc.select("table").get(0); //select the first table.
        Elements rows = table.select("tr");
        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cols = row.select("td");
            metadata.put(cols.get(1).text(), cols.get(2).text());
        }
        String version = metadata.get("snapshotVersion");
        if (StringUtil.isBlank(version)) {
            artifact.setSnapsort(false);
            version = metadata.get("version");
        } else {
            artifact.setSnapsort(true);
        }
        //Problem solved for artifact id
        String artifactId = null;
        List<String> nameKeys = Arrays.asList("artifactId", "name", "basename");
        Iterator<String> it = nameKeys.iterator();
        while (it.hasNext()) {
            artifactId = metadata.get(it.next());
            if (!StringUtil.isBlank(artifactId)) {
                break;
            }
        }
        if (StringUtil.isBlank(version) | StringUtil.isBlank(artifactId)) {
            // We ignore which have no version or no artifactid
            return null;
        }
        // some of the Extensions are not in the meta-data
        String extension = metadata.get("extension");
        if (StringUtil.isBlank(extension)) {
            extension = FilenameUtils.getExtension(doc.select("h3.details-heading").first().text().split(" ")[0]);
        }
        artifact.setArtifactId(artifactId);
        if (!version.contains("v") | !version.contains("-")) {
            String[] sp = version.split(dot);
            if (sp.length == 4) {
                version = sp[0] + "." + sp[1] + "." + sp[2] + "-" + sp[3];
            }
        }
        artifact.setArtifactVersion(new DefaultArtifactVersion(version));

        artifact.setChannelId(channelId);
        artifact.setClassifier(metadata.get("artifactLabel"));
        artifact.setExtension(extension);
        artifact.setDataArtifactId(dataArtifactId);
        return artifact;
    }

    public Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).cookies(this.cookies).maxBodySize(0).get();
    }

    public boolean deleteArtifact(Artifact artifact) throws IOException {
        Connection.Response res = Jsoup.connect(this.host + "/channel/" + artifact.getChannelId() + "/artifacts/" + artifact.getDataArtifactId() + "/delete").cookies(this.cookies).maxBodySize(0).execute();
        return HttpResponseValidator.validateResponse(res);
    }

    public void cleanChannel(String channelId) throws IOException {
        List<Artifact> atrifacts = getArtifacts(channelId);
        Map<String, List<Artifact>> groupByArtMap = atrifacts.stream().collect(Collectors.groupingBy(Artifact::getGroupingString));
        Map<String, List<Artifact>> map = FilterUtil.filterbyGroupWithSingleElement(groupByArtMap);
        for (Entry<String, List<Artifact>> entry : map.entrySet()) {
            List<Artifact> atrifacts1 = entry.getValue();
            Collections.sort(atrifacts1);
            System.out.println();
            for (int i = 0; i < atrifacts1.size(); i++) {
                if (i < atrifacts1.size() - 1) {
                    System.out.println("Delete:" + atrifacts1.get(i));
                    deleteArtifact(atrifacts1.get(i));
                } else {
                    System.out.println("Keep:" + atrifacts1.get(i));
                }
            }
            System.out.println();
        }
    }

}
