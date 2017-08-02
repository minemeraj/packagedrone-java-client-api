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
package de.dsa.packagedrone.client.model;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class Artifact implements Comparable<Artifact> {
    private String channelId;
    private String artifactId;
    private String classifier;
    private String extension;
    private String dataArtifactId;
    private DefaultArtifactVersion artifactVersion;
    private boolean snapsort;

    public boolean isSnapsort() {
        return snapsort;
    }

    public void setSnapsort(boolean snapsort) {
        this.snapsort = snapsort;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getDataArtifactId() {
        return dataArtifactId;
    }

    public void setDataArtifactId(String dataArtifactId) {
        this.dataArtifactId = dataArtifactId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public DefaultArtifactVersion getArtifactVersion() {
        return artifactVersion;
    }

    public void setArtifactVersion(DefaultArtifactVersion artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    @Override
    public int compareTo(Artifact artifact) {
        return this.artifactVersion.compareTo(artifact.getArtifactVersion());
    }

    @Override
    public String toString() {
        return this.channelId + "|" + this.artifactId + "|" + this.classifier + "|" + this.extension + "|" + this.dataArtifactId + "|" + this.artifactVersion + "|" + this.snapsort;
    }

    public String getGroupingString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getArtifactId());
        buffer.append("-");
        buffer.append(getClassifier());
        buffer.append("-");
        if (getArtifactVersion().getQualifier() != null) {
            buffer.append(getArtifactVersion().toString().replace("-" + getArtifactVersion().getQualifier(), ""));
        } else {
            buffer.append(getArtifactVersion());
        }
        buffer.append("-");
        buffer.append(getExtension());
        return buffer.toString();
    }
}
