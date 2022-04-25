package com.github.samirromdhani.jaxblib.commons.jaxb;

import lombok.extern.java.Log;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.*;

@Log
public class GoodJAXBUtilGeneric {

    public byte[] marshal(Object ob) {
        JAXBContext jaxbContext = JaxbContextHolder.getJAXBContext(ob);
        
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos)){
        
            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(ob, bos);
            bos.flush();
            return baos.toByteArray();
        } catch (JAXBException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    public <T> T unmarshal(Class<T> c,Reader reader) {
        try {
            InputSource inputSource = new InputSource(reader);
            Object ob = unmarshal(c, inputSource);
            return c.cast(ob);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        } 
    }
    
    public <T> T unmarshal(Class<T> c,InputStream inputStream) {
        try {
            InputSource inputSource = new InputSource(inputStream);
            Object ob = unmarshal(c, inputSource);
            return c.cast(ob);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public <T> T unmarshal(Class<T> c,byte[] bytes) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            BufferedInputStream bis = new BufferedInputStream(bais)){
            return unmarshal(c,bis);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } 
    }
    
    private <T> Object unmarshal(Class<T> c, InputSource inputSource) throws JAXBException {
        try {
            return unmarshalWithSAX(c, inputSource);
        } catch (ParserConfigurationException | SAXException | JAXBException ex) {
            return unmarshalDefault(c, inputSource);
        }
    }
    
    private <T> Object unmarshalDefault(Class<T> c,InputSource inputSource) throws JAXBException {
        JAXBContext jaxbContext = JaxbContextHolder.getJAXBContext(c);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        return u.unmarshal(inputSource);
    }   
    
    private <T> Object unmarshalWithSAX(Class<T> c, InputSource inputSource) throws ParserConfigurationException, SAXException, JAXBException{
        JAXBContext jaxbContext = JaxbContextHolder.getJAXBContext(c);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(true);
        
        SAXParser saxParser = spf.newSAXParser();
        saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        
        XMLReader xmlReader = saxParser.getXMLReader();
        SAXSource source = new SAXSource( xmlReader, inputSource);

        Unmarshaller u = jaxbContext.createUnmarshaller();

        return u.unmarshal( source );
    }
   
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    
}
