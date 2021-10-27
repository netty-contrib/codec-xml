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

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the basic functionality of the {@link XmlDecoder}.
 * XML borrowed from
 * <a href="https://www.studytrails.com/java/xml/woodstox/java-xml-woodstox-validation-xml-schema.jsp">
 * Woodstox : Validate against XML Schema</a>
 */
@SuppressWarnings("OverlyStrongTypeCast")
public class XmlDecoderTest {

    private static final String XML1 = "<?xml version=\"1.0\"?>" +
            "<!DOCTYPE employee SYSTEM \"employee.dtd\">" +
            "<?xml-stylesheet type=\"text/css\" href=\"netty.css\"?>" +
            "<?xml-test ?>" +
            "<employee xmlns:nettya=\"https://netty.io/netty/a\">" +
            "<nettya:id>&plusmn;1</nettya:id>\n" +
            "<name ";

    private static final String XML2 = "type=\"given\">Alba</name><![CDATA[ <some data &gt;/> ]]>" +
            "   <!-- namespaced --><nettyb:salary xmlns:nettyb=\"https://netty.io/netty/b\" nettyb:period=\"weekly\">" +
            "100</nettyb:salary><last/></employee>";

    private static final String XML3 = "<?xml version=\"1.1\" encoding=\"UTf-8\" standalone=\"yes\"?><netty></netty>";

    private static final String XML4 = "<netty></netty>";

    private EmbeddedChannel channel;

    @BeforeEach
    public void setup() throws Exception {
        channel = new EmbeddedChannel(new XmlDecoder());
    }

    @AfterEach
    public void teardown() throws Exception {
        channel.finish();
    }

    /**
     * This test feeds basic XML and verifies the resulting messages
     */
    @Test
    public void shouldDecodeRequestWithSimpleXml() {
        Object temp;

        write(XML1);

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlDocumentStart.class);
        assertThat(((XmlDocumentStart) temp).version()).isEqualTo("1.0");
        assertThat(((XmlDocumentStart) temp).encoding()).isEqualTo("UTF-8");
        assertThat(((XmlDocumentStart) temp).standalone()).isFalse();
        assertThat(((XmlDocumentStart) temp).encodingScheme()).isNull();

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlDTD.class);
        assertThat(((XmlDTD) temp).text()).isEqualTo("employee.dtd");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlProcessingInstruction.class);
        assertThat(((XmlProcessingInstruction) temp).target()).isEqualTo("xml-stylesheet");
        assertThat(((XmlProcessingInstruction) temp).data()).isEqualTo("type=\"text/css\" href=\"netty.css\"");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlProcessingInstruction.class);
        assertThat(((XmlProcessingInstruction) temp).target()).isEqualTo("xml-test");
        assertThat(((XmlProcessingInstruction) temp).data()).isEqualTo("");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementStart.class);
        assertThat(((XmlElementStart) temp).name()).isEqualTo("employee");
        assertThat(((XmlElementStart) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementStart) temp).namespace()).isEqualTo("");
        assertThat(((XmlElementStart) temp).attributes().size()).isEqualTo(0);
        assertThat(((XmlElementStart) temp).namespaces().size()).isEqualTo(1);
        assertThat(((XmlElementStart) temp).namespaces().get(0).prefix()).isEqualTo("nettya");
        assertThat(((XmlElementStart) temp).namespaces().get(0).uri()).isEqualTo("https://netty.io/netty/a");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementStart.class);
        assertThat(((XmlElementStart) temp).name()).isEqualTo("id");
        assertThat(((XmlElementStart) temp).prefix()).isEqualTo("nettya");
        assertThat(((XmlElementStart) temp).namespace()).isEqualTo("https://netty.io/netty/a");
        assertThat(((XmlElementStart) temp).attributes().size()).isZero();
        assertThat(((XmlElementStart) temp).namespaces().size()).isZero();

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlEntityReference.class);
        assertThat(((XmlEntityReference) temp).name()).isEqualTo("plusmn");
        assertThat(((XmlEntityReference) temp).text()).isEqualTo("");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlCharacters.class);
        assertThat(((XmlCharacters) temp).data()).isEqualTo("1");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementEnd.class);
        assertThat(((XmlElementEnd) temp).name()).isEqualTo("id");
        assertThat(((XmlElementEnd) temp).prefix()).isEqualTo("nettya");
        assertThat(((XmlElementEnd) temp).namespace()).isEqualTo("https://netty.io/netty/a");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlCharacters.class);
        assertThat(((XmlCharacters) temp).data()).isEqualTo("\n");

        temp = channel.readInbound();
        assertThat(temp).isNull();

        write(XML2);

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementStart.class);
        assertThat(((XmlElementStart) temp).name()).isEqualTo("name");
        assertThat(((XmlElementStart) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementStart) temp).namespace()).isEqualTo("");
        assertThat(((XmlElementStart) temp).attributes().size()).isOne();
        assertThat(((XmlElementStart) temp).attributes().get(0).name()).isEqualTo("type");
        assertThat(((XmlElementStart) temp).attributes().get(0).value()).isEqualTo("given");
        assertThat(((XmlElementStart) temp).attributes().get(0).prefix()).isEqualTo("");
        assertThat(((XmlElementStart) temp).attributes().get(0).namespace()).isEqualTo("");
        assertThat(((XmlElementStart) temp).namespaces().size()).isZero();

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlCharacters.class);
        assertThat(((XmlCharacters) temp).data()).isEqualTo("Alba");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementEnd.class);
        assertThat(((XmlElementEnd) temp).name()).isEqualTo("name");
        assertThat(((XmlElementEnd) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementEnd) temp).namespace()).isEqualTo("");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlCdata.class);
        assertThat(((XmlCdata) temp).data()).isEqualTo(" <some data &gt;/> ");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlCharacters.class);
        assertThat(((XmlCharacters) temp).data()).isEqualTo("   ");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlComment.class);
        assertThat(((XmlComment) temp).data()).isEqualTo(" namespaced ");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementStart.class);
        assertThat(((XmlElementStart) temp).name()).isEqualTo("salary");
        assertThat(((XmlElementStart) temp).prefix()).isEqualTo("nettyb");
        assertThat(((XmlElementStart) temp).namespace()).isEqualTo("https://netty.io/netty/b");
        assertThat(((XmlElementStart) temp).attributes().size()).isOne();
        assertThat(((XmlElementStart) temp).attributes().get(0).name()).isEqualTo("period");
        assertThat(((XmlElementStart) temp).attributes().get(0).value()).isEqualTo("weekly");
        assertThat(((XmlElementStart) temp).attributes().get(0).prefix()).isEqualTo("nettyb");
        assertThat(((XmlElementStart) temp).attributes().get(0).namespace()).isEqualTo("https://netty.io/netty/b");
        assertThat(((XmlElementStart) temp).namespaces().size()).isOne();
        assertThat(((XmlElementStart) temp).namespaces().get(0).prefix()).isEqualTo("nettyb");
        assertThat(((XmlElementStart) temp).namespaces().get(0).uri()).isEqualTo("https://netty.io/netty/b");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlCharacters.class);
        assertThat(((XmlCharacters) temp).data()).isEqualTo("100");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementEnd.class);
        assertThat(((XmlElementEnd) temp).name()).isEqualTo("salary");
        assertThat(((XmlElementEnd) temp).prefix()).isEqualTo("nettyb");
        assertThat(((XmlElementEnd) temp).namespace()).isEqualTo("https://netty.io/netty/b");
        assertThat(((XmlElementEnd) temp).namespaces().size()).isOne();
        assertThat(((XmlElementEnd) temp).namespaces().get(0).prefix()).isEqualTo("nettyb");
        assertThat(((XmlElementEnd) temp).namespaces().get(0).uri()).isEqualTo("https://netty.io/netty/b");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementStart.class);
        assertThat(((XmlElementStart) temp).name()).isEqualTo("last");
        assertThat(((XmlElementStart) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementStart) temp).namespace()).isEqualTo("");
        assertThat(((XmlElementStart) temp).attributes().size()).isZero();
        assertThat(((XmlElementStart) temp).namespaces().size()).isZero();

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementEnd.class);
        assertThat(((XmlElementEnd) temp).name()).isEqualTo("last");
        assertThat(((XmlElementEnd) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementEnd) temp).namespace()).isEqualTo("");
        assertThat(((XmlElementEnd) temp).namespaces().size()).isZero();

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementEnd.class);
        assertThat(((XmlElementEnd) temp).name()).isEqualTo("employee");
        assertThat(((XmlElementEnd) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementEnd) temp).namespace()).isEqualTo("");
        assertThat(((XmlElementEnd) temp).namespaces().size()).isOne();
        assertThat(((XmlElementEnd) temp).namespaces().get(0).prefix()).isEqualTo("nettya");
        assertThat(((XmlElementEnd) temp).namespaces().get(0).uri()).isEqualTo("https://netty.io/netty/a");

        temp = channel.readInbound();
        assertThat(temp).isNull();
    }

    /**
     * This test checks for different attributes in XML header
     */
    @Test
    public void shouldDecodeXmlHeader() {
        Object temp;

        write(XML3);

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlDocumentStart.class);
        assertThat(((XmlDocumentStart) temp).version()).isEqualTo("1.1");
        assertThat(((XmlDocumentStart) temp).encoding()).isEqualTo("UTF-8");
        assertThat(((XmlDocumentStart) temp).standalone()).isEqualTo(true);
        assertThat(((XmlDocumentStart) temp).encodingScheme()).isEqualTo("UTF-8");

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementStart.class);
        assertThat(((XmlElementStart) temp).name()).isEqualTo("netty");
        assertThat(((XmlElementStart) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementStart) temp).namespace()).isEqualTo("");
        assertThat(((XmlElementStart) temp).attributes().size()).isZero();
        assertThat(((XmlElementStart) temp).namespaces().size()).isZero();

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementEnd.class);
        assertThat(((XmlElementEnd) temp).name()).isEqualTo("netty");
        assertThat(((XmlElementEnd) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementEnd) temp).namespace()).isEqualTo("");
        assertThat(((XmlElementEnd) temp).namespaces().size()).isZero();

        temp = channel.readInbound();
        assertThat(temp).isNull();
    }

    /**
     * This test checks for no XML header
     */
    @Test
    public void shouldDecodeWithoutHeader() {
        Object temp;

        write(XML4);

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlDocumentStart.class);
        assertThat(((XmlDocumentStart) temp).version()).isNull();
        assertThat(((XmlDocumentStart) temp).encoding()).isEqualTo("UTF-8");
        assertThat(((XmlDocumentStart) temp).standalone()).isEqualTo(false);
        assertThat(((XmlDocumentStart) temp).encodingScheme()).isNull();

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementStart.class);
        assertThat(((XmlElementStart) temp).name()).isEqualTo("netty");
        assertThat(((XmlElementStart) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementStart) temp).namespace()).isEqualTo("");
        assertThat(((XmlElementStart) temp).attributes().size()).isZero();
        assertThat(((XmlElementStart) temp).namespaces().size()).isZero();

        temp = channel.readInbound();
        assertThat(temp).isInstanceOf(XmlElementEnd.class);
        assertThat(((XmlElementEnd) temp).name()).isEqualTo("netty");
        assertThat(((XmlElementEnd) temp).prefix()).isEqualTo("");
        assertThat(((XmlElementEnd) temp).namespace()).isEqualTo("");
        assertThat(((XmlElementEnd) temp).namespaces().size()).isZero();

        temp = channel.readInbound();
        assertThat(temp).isNull();
    }

    private void write(String content) {
        assertThat(channel.writeInbound(Unpooled.copiedBuffer(content, CharsetUtil.UTF_8))).isEqualTo(true);
    }

}
