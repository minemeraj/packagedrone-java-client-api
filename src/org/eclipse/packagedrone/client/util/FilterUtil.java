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
 *    Created on  5 Jan 2017
 *
 ************************************************************************/
package org.eclipse.packagedrone.client.util;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.client.model.Artifact;

public class FilterUtil {
    public static List<Artifact> filturebyExtension(List<Artifact> atrifacts, String extension) {
        return atrifacts.parallelStream().filter(atrifact -> atrifact.getExtension().equals(extension)).collect(Collectors.toList());
    }

    public static Map<String, List<Artifact>> filterbyGroupWithSingleElement(Map<String, List<Artifact>> map) {
        Map<String, List<Artifact>> artifactMap = map.entrySet().parallelStream().filter(artifactGroup -> artifactGroup.getValue().size() > 1)
            .collect(Collectors.toMap(artifactGroup -> artifactGroup.getKey(), artifactGroup -> artifactGroup.getValue()));
        return new TreeMap<String, List<Artifact>>(artifactMap);
    }
}
