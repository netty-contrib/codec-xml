/*
 * Copyright 2021 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.contrib.handler.codec.xml;

/**
 * Beginning of the XML document ... i.e. XML header
 */
public class XmlDocumentStart {

    private final String encoding;
    private final String version;
    private final boolean standalone;
    private final String encodingScheme;

    public XmlDocumentStart(String encoding, String version, boolean standalone, String encodingScheme) {
        this.encoding = encoding;
        this.version = version;
        this.standalone = standalone;
        this.encodingScheme = encodingScheme;
    }

    /**
     * Return defined or guessed XML encoding
     *
     * @return XMl encoding
     **/
    public String encoding() {
        return encoding;
    }

    /**
     * Return defined XML version or null
     *
     * @return XML version
     **/
    public String version() {
        return version;
    }

    /**
     * Return if the document is standalone or not
     *
     * @return {@code true} if the document is standalone
     **/
    public boolean standalone() {
        return standalone;
    }

    /**
     * Return defined encoding or null
     *
     * @return encoding scheme
     **/
    public String encodingScheme() {
        return encodingScheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XmlDocumentStart that = (XmlDocumentStart) o;

        if (standalone != that.standalone) {
            return false;
        }
        if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) {
            return false;
        }
        if (encodingScheme != null ? !encodingScheme.equals(that.encodingScheme) : that.encodingScheme != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = encoding != null ? encoding.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (standalone ? 1 : 0);
        result = 31 * result + (encodingScheme != null ? encodingScheme.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "XmlDocumentStart{" +
                "encoding='" + encoding + '\'' +
                ", version='" + version + '\'' +
                ", standalone=" + standalone +
                ", encodingScheme='" + encodingScheme + '\'' +
                '}';
    }
}
