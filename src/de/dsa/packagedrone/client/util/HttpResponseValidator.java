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
package de.dsa.packagedrone.client.util;

import org.apache.http.client.HttpResponseException;
import org.jsoup.Connection;

public class HttpResponseValidator {

    public static boolean validateResponse(Connection.Response res) throws HttpResponseException {
        if (res.statusCode() < 200 || res.statusCode() >= 400) {
            throw new HttpResponseException(res.statusCode(), res.statusMessage());
        }
        return true;
    }
}
