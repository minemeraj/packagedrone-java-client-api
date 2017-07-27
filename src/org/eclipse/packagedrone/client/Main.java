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
 *    Created on  21 Dec 2016
 *
 ************************************************************************/
package org.eclipse.packagedrone.client;

public class Main {
    public static final String HOST = "";
    public static final String USERNAME = "";
    public static final String PASSWORD = "";

    public static void main(String[] args) {
        try {
            PackageDroneServer droneServer = new PackageDroneServer(HOST, USERNAME, PASSWORD);
            for (String string : args) {                
                droneServer.cleanChannel(string);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
