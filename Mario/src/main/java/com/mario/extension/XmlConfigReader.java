package com.mario.extension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.nhb.common.BaseLoggable;

public abstract class XmlConfigReader extends BaseLoggable {

	protected static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	protected static final XPath xPath = XPathFactory.newInstance().newXPath();

	public void read(String path) throws Exception {
		this.read(new File(path));
	}

	public void read(File file) throws Exception {
		try (InputStream is = new FileInputStream(file)) {
			this.read(is);
		}
	}

	public void read(InputStream is) throws Exception {
		try (StringWriter sw = new StringWriter()) {
			IOUtils.copy(is, sw);
			this.readXml(sw.toString());
		}
	}

	public void readXml(String xml) throws Exception {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
		this.read(document);
	}

	protected abstract void read(Document document) throws Exception;
}
