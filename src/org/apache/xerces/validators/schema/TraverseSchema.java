
/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xerces.validators.schema;

import  org.apache.xerces.framework.XMLErrorReporter;
import  org.apache.xerces.validators.schema.SchemaSymbols;
import  org.apache.xerces.validators.schema.XUtil;
import  org.apache.xerces.validators.datatype.DatatypeValidator;
import  org.apache.xerces.utils.StringPool;
import  org.w3c.dom.Element;

//REVISIT: for now, import everything in the DOM package
import  org.w3c.dom.*;
import java.util.*;

//Unit Test 
import  org.apache.xerces.parsers.DOMParser;
import  org.apache.xerces.validators.common.XMLValidator;
import  org.apache.xerces.validators.datatype.DatatypeValidator;
import  org.apache.xerces.validators.datatype.InvalidDatatypeValueException;
import  org.apache.xerces.framework.XMLContentSpec;
import  org.apache.xerces.utils.QName;
import  org.apache.xerces.parsers.SAXParser;
import  org.apache.xerces.framework.XMLParser;
import  org.apache.xerces.framework.XMLDocumentScanner;
import org.apache.xerces.readers.DefaultEntityHandler;
import org.apache.xerces.readers.XMLDeclRecognizer;
import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.readers.XMLEntityReaderFactory;
import org.apache.xerces.utils.ChunkyCharArray;

import  org.xml.sax.InputSource;
import  org.xml.sax.SAXParseException;
import  org.xml.sax.EntityResolver;
import  org.xml.sax.ErrorHandler;
import  org.xml.sax.SAXException;
import  java.io.IOException;
import  org.w3c.dom.Document;
import  org.apache.xml.serialize.OutputFormat;
import  org.apache.xml.serialize.XMLSerializer;




/**
 * Instances of this class get delegated to Traverse the Schema and
 * to populate the Grammar internal representation by
 * instances of Grammar objects.
 * Traverse a Schema Grammar:
     * As of April 07, 2000 the following is the
     * XML Representation of Schemas and Schema components,
     * Chapter 4 of W3C Working Draft.
     * <schema 
     *   attributeFormDefault = qualified | unqualified 
     *   blockDefault = #all or (possibly empty) subset of {equivClass, extension, restriction} 
     *   elementFormDefault = qualified | unqualified 
     *   finalDefault = #all or (possibly empty) subset of {extension, restriction} 
     *   id = ID 
     *   targetNamespace = uriReference 
     *   version = string>
     *   Content: ((include | import | annotation)* , ((simpleType | complexType | element | group | attribute | attributeGroup | notation) , annotation*)+)
     * </schema>
     * 
     * 
     * <attribute 
     *   form = qualified | unqualified 
     *   id = ID 
     *   name = NCName 
     *   ref = QName 
     *   type = QName 
     *   use = default | fixed | optional | prohibited | required 
     *   value = string>
     *   Content: (annotation? , simpleType?)
     * </>
     * 
     * <element 
     *   abstract = boolean 
     *   block = #all or (possibly empty) subset of {equivClass, extension, restriction} 
     *   default = string 
     *   equivClass = QName 
     *   final = #all or (possibly empty) subset of {extension, restriction} 
     *   fixed = string 
     *   form = qualified | unqualified 
     *   id = ID 
     *   maxOccurs = string 
     *   minOccurs = nonNegativeInteger 
     *   name = NCName 
     *   nullable = boolean 
     *   ref = QName 
     *   type = QName>
     *   Content: (annotation? , (simpleType | complexType)? , (unique | key | keyref)*)
     * </>
     * 
     * 
     * <complexType 
     *   abstract = boolean 
     *   base = QName 
     *   block = #all or (possibly empty) subset of {extension, restriction} 
     *   content = elementOnly | empty | mixed | textOnly 
     *   derivedBy = extension | restriction 
     *   final = #all or (possibly empty) subset of {extension, restriction} 
     *   id = ID 
     *   name = NCName>
     *   Content: (annotation? , (((minExclusive | minInclusive | maxExclusive | maxInclusive | precision | scale | length | minLength | maxLength | encoding | period | duration | enumeration | pattern)* | (element | group | all | choice | sequence | any)*) , ((attribute | attributeGroup)* , anyAttribute?)))
     * </>
     * 
     * 
     * <attributeGroup 
     *   id = ID 
     *   name = NCName
     *   ref = QName>
     *   Content: (annotation?, (attribute|attributeGroup), anyAttribute?)
     * </>
     * 
     * <anyAttribute 
     *   id = ID 
     *   namespace = ##any | ##other | ##local | list of {uri, ##targetNamespace}>
     *   Content: (annotation?)
     * </anyAttribute>
     * 
     * <group 
     *   id = ID 
     *   maxOccurs = string 
     *   minOccurs = nonNegativeInteger 
     *   name = NCName 
     *   ref = QName>
     *   Content: (annotation? , (element | group | all | choice | sequence | any)*)
     * </>
     * 
     * <all 
     *   id = ID 
     *   maxOccurs = string 
     *   minOccurs = nonNegativeInteger>
     *   Content: (annotation? , (element | group | choice | sequence | any)*)
     * </all>
     * 
     * <choice 
     *   id = ID 
     *   maxOccurs = string 
     *   minOccurs = nonNegativeInteger>
     *   Content: (annotation? , (element | group | choice | sequence | any)*)
     * </choice>
     * 
     * <sequence 
     *   id = ID 
     *   maxOccurs = string 
     *   minOccurs = nonNegativeInteger>
     *   Content: (annotation? , (element | group | choice | sequence | any)*)
     * </sequence>
     * 
     * 
     * <any 
     *   id = ID 
     *   maxOccurs = string 
     *   minOccurs = nonNegativeInteger 
     *   namespace = ##any | ##other | ##local | list of {uri, ##targetNamespace} 
     *   processContents = lax | skip | strict>
     *   Content: (annotation?)
     * </any>
     * 
     * <unique 
     *   id = ID 
     *   name = NCName>
     *   Content: (annotation? , (selector , field+))
     * </unique>
     * 
     * <key 
     *   id = ID 
     *   name = NCName>
     *   Content: (annotation? , (selector , field+))
     * </key>
     * 
     * <keyref 
     *   id = ID 
     *   name = NCName 
     *   refer = QName>
     *   Content: (annotation? , (selector , field+))
     * </keyref>
     * 
     * <selector>
     *   Content: XPathExprApprox : An XPath expression 
     * </selector>
     * 
     * <field>
     *   Content: XPathExprApprox : An XPath expression 
     * </field>
     * 
     * 
     * <notation 
     *   id = ID 
     *   name = NCName 
     *   public = A public identifier, per ISO 8879 
     *   system = uriReference>
     *   Content: (annotation?)
     * </notation>
     * 
     * <annotation>
     *   Content: (appinfo | documentation)*
     * </annotation>
     * 
     * <include 
     *   id = ID 
     *   schemaLocation = uriReference>
     *   Content: (annotation?)
     * </include>
     * 
     * <import 
     *   id = ID 
     *   namespace = uriReference 
     *   schemaLocation = uriReference>
     *   Content: (annotation?)
     * </import>
     * 
     * <simpleType
     *   abstract = boolean 
     *   base = QName 
     *   derivedBy = | list | restriction  : restriction
     *   id = ID 
     *   name = NCName>
     *   Content: ( annotation? , ( minExclusive | minInclusive | maxExclusive | maxInclusive | precision | scale | length | minLength | maxLength | encoding | period | duration | enumeration | pattern )* )
     * </simpleType>
     * 
     * <length
     *   id = ID 
     *   value = nonNegativeInteger>
     *   Content: ( annotation? )
     * </length>
     * 
     * <minLength
     *   id = ID 
     *   value = nonNegativeInteger>
     *   Content: ( annotation? )
     * </minLength>
     * 
     * <maxLength
     *   id = ID 
     *   value = nonNegativeInteger>
     *   Content: ( annotation? )
     * </maxLength>
     * 
     * 
     * <pattern
     *   id = ID 
     *   value = string>
     *   Content: ( annotation? )
     * </pattern>
     * 
     * 
     * <enumeration
     *   id = ID 
     *   value = string>
     *   Content: ( annotation? )
     * </enumeration>
     * 
     * <maxInclusive
     *   id = ID 
     *   value = string>
     *   Content: ( annotation? )
     * </maxInclusive>
     * 
     * <maxExclusive
     *   id = ID 
     *   value = string>
     *   Content: ( annotation? )
     * </maxExclusive>
     * 
     * <minInclusive
     *   id = ID 
     *   value = string>
     *   Content: ( annotation? )
     * </minInclusive>
     * 
     * 
     * <minExclusive
     *   id = ID 
     *   value = string>
     *   Content: ( annotation? )
     * </minExclusive>
     * 
     * <precision
     *   id = ID 
     *   value = nonNegativeInteger>
     *   Content: ( annotation? )
     * </precision>
     * 
     * <scale
     *   id = ID 
     *   value = nonNegativeInteger>
     *   Content: ( annotation? )
     * </scale>
     * 
     * <encoding
     *   id = ID 
     *   value = | hex | base64 >
     *   Content: ( annotation? )
     * </encoding>
     * 
     * 
     * <duration
     *   id = ID 
     *   value = timeDuration>
     *   Content: ( annotation? )
     * </duration>
     * 
     * <period
     *   id = ID 
     *   value = timeDuration>
     *   Content: ( annotation? )
     * </period>
     * 
 * 
 * @author Jeffrey Rodriguez
 *         Eric Ye
 * @see                  org.apache.xerces.validators.common.Grammar
 */

public class TraverseSchema {

    /**
     * 
     * @param root
     * @exception Exception
     */
    private XMLErrorReporter    fErrorReporter = null;
    private StringPool          fStringPool    = null;

    //REVISIT: fValidator needs to be initialized somehow
    private XMLValidator        fValidator     = null;
    protected DefaultEntityHandler fEntityHandler = null;
    protected XMLDocumentScanner fScanner = null;

    private Element fSchemaRootElement;
    private NodeList fGlobalGroups;
    private NodeList fGlobalAttrs;
    private NodeList fGlobalAttrGrps;

    private DatatypeValidatorRegistry fDatatypeRegistry = new DatatypeValidatorRegistry();
    private Hashtable fComplexTypeRegistry = new Hashtable();

    private int fAnonTypeCount =0;
    private int fScopeCount=0;
    private int fCurrentScope=0;

    private boolean defaultQualified = false;
    private int targetNSURI;
    private String targetNSUriString = "";

    class ComplexTypeInfo {
        public String typeName;
        
        public String base;
        public int derivedBy;
        public int blockSet;
        public int finalSet;

        public int scopeDefined = -1;

        public int contentType;
        public int contentSpecHandle = -1;
        public int attlistHead = -1;
        public DatatypeValidator datatypeValidator;
    }


    //REVISIT: verify the URI.
    public final static String SchemaForSchemaURI = "http://www.w3.org/TR-1/Schema";


    public  TraverseSchema(Element root ) throws Exception {
        
        fSchemaRootElement = root;

        fStringPool = new StringPool();
        fErrorReporter = new SAXParser();
        fEntityHandler = new DefaultEntityHandler(fStringPool, fErrorReporter);
        fScanner = new XMLDocumentScanner(fStringPool, fErrorReporter, fEntityHandler, new ChunkyCharArray(fStringPool));
        fValidator = new XMLValidator(fStringPool,fErrorReporter,fEntityHandler,fScanner);

        if (root == null) { // Anything to do?
            return;
        }

        //Retrieve the targetnamespace URI information
        targetNSUriString = root.getAttribute(SchemaSymbols.ATT_TARGETNAMESPACE);
        if (targetNSUriString==null) {
            targetNSUriString="";
        }
        targetNSURI = fStringPool.addSymbol(targetNSUriString);

        defaultQualified = 
            root.getAttribute(SchemaSymbols.ATT_ELEMENTFORMDEFAULT).equals(SchemaSymbols.ATTVAL_QUALIFIED);

        fScopeCount++;
        fCurrentScope = 0;

        //fGlobalGroups = XUtil.getChildElementsByTagNameNS(root,SchemaForSchemaURI,SchemaSymbols.ELT_GROUP);
        //fGlobalAttrs  = XUtil.getChildElementsByTagNameNS(root,SchemaForSchemaURI,SchemaSymbols.ELT_ATTRIBUTE);
        //fGlobalAttrGrps = XUtil.getChildElementsByTagNameNS(root,SchemaForSchemaURI,SchemaSymbols.ELT_ATTRIBUTEGROUP);

        checkTopLevelDuplicateNames(root);

        for (Element child = XUtil.getFirstChildElement(root); child != null;
            child = XUtil.getNextSiblingElement(child)) {

            String name = child.getNodeName();

            if (name.equals(SchemaSymbols.ELT_ANNOTATION) ) {
                traverseAnnotationDecl(child);
            } else if (name.equals(SchemaSymbols.ELT_SIMPLETYPE )) {
                traverseSimpleTypeDecl(child);
            } else if (name.equals(SchemaSymbols.ELT_COMPLEXTYPE )) {
                traverseComplexTypeDecl(child);
            } else if (name.equals(SchemaSymbols.ELT_ELEMENT )) { // && child.getAttribute(SchemaSymbols.ATT_REF).equals("")) {
                traverseElementDecl(child);
            } else if (name.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                //traverseAttributeGroupDecl(child);
            } else if (name.equals( SchemaSymbols.ELT_ATTRIBUTE ) ) {
                //traverseAttributeDecl( child );
            } else if (name.equals( SchemaSymbols.ELT_WILDCARD) ) {
                traverseWildcardDecl( child);
            } else if (name.equals(SchemaSymbols.ELT_GROUP) && child.getAttribute(SchemaSymbols.ATT_REF).equals("")) {
                //traverseGroupDecl(child);
            } else if (name.equals(SchemaSymbols.ELT_NOTATION)) {
                ;
            }
        } // for each child node
    } // traverseSchema(Element)

    private void checkTopLevelDuplicateNames(Element root) {
    }

    /**
     * No-op - Traverse Annotation Declaration
     * 
     * @param comment
     */
    private void traverseAnnotationDecl(Element comment) {
        return ;
    }

    /**
     * Traverse SimpleType declaration:
     * <simpleType
     *         abstract = boolean 
     *         base = QName 
     *         derivedBy = | list | restriction  : restriction
     *         id = ID 
     *         name = NCName>
     *         Content: ( annotation? , ( minExclusive | minInclusive | maxExclusive | maxInclusive | precision | scale | length | minLength | maxLength | encoding | period | duration | enumeration | pattern )* )
     *       </simpleType>
     * 
     * @param simpleTypeDecl
     * @return 
     */
    private int traverseSimpleTypeDecl( Element simpleTypeDecl ) {
        int simpleTypeAbstract   =  fStringPool.addSymbol(
                                                         simpleTypeDecl.getAttribute( SchemaSymbols.ATT_ABSTRACT ));

        int simpleTypeBasetype   = fStringPool.addSymbol(
                                                        simpleTypeDecl.getAttribute( SchemaSymbols.ATT_BASE ));

        int simpleTypeDerivedBy  =  fStringPool.addSymbol(
                                                         simpleTypeDecl.getAttribute( SchemaSymbols.ATT_DERIVEDBY ));

        int simpleTypeID         =  fStringPool.addSymbol(
                                                         simpleTypeDecl.getAttribute( SchemaSymbols.ATTVAL_ID ));

        int simpleTypeName       =  fStringPool.addSymbol(
                                                         simpleTypeDecl.getAttribute( SchemaSymbols.ATT_NAME ));

        Element simpleTypeChild = XUtil.getFirstChildElement(simpleTypeDecl);

        // check that base type is defined
        //REVISIT: how do we do the extension mechanism? hardwired type name?

        //DatatypeValidator baseValidator = 
       // fDatatypeRegistry.getValidatorFor(simpleTypeChild.getAttribute(ATT_NAME));
        //if (baseValidator == null) {
         //   reportSchemaError(SchemaMessageProvider.UnknownBaseDatatype,
          //                    new Object [] { simpleTypeChild.getAttribute(ATT_NAME), simpleTypeDecl.getAttribute(ATT_NAME)});

        //    return -1;
        //}

        // build facet list

        // create & register validator for "generated" type if it doesn't exist
        return -1;
    }


    /**
     * Traverse ComplexType Declaration.
     *  
     *       <complexType 
     *         abstract = boolean 
     *         base = QName 
     *         block = #all or (possibly empty) subset of {extension, restriction} 
     *         content = elementOnly | empty | mixed | textOnly 
     *         derivedBy = extension | restriction 
     *         final = #all or (possibly empty) subset of {extension, restriction} 
     *         id = ID 
     *         name = NCName>
     *          Content: (annotation? , (((minExclusive | minInclusive | maxExclusive
     *                    | maxInclusive | precision | scale | length | minLength 
     *                    | maxLength | encoding | period | duration | enumeration 
     *                    | pattern)* | (element | group | all | choice | sequence | any)*) , 
     *                    ((attribute | attributeGroup)* , anyAttribute?)))
     *        </complexType>
     * @param complexTypeDecl
     * @return 
     */
    
    //REVISIT: TO DO, base and derivation ???
    private int traverseComplexTypeDecl( Element complexTypeDecl ) throws Exception{ 
        int complexTypeAbstract  = fStringPool.addSymbol(
                                                        complexTypeDecl.getAttribute( SchemaSymbols.ATT_ABSTRACT ));
        String isAbstract = complexTypeDecl.getAttribute( SchemaSymbols.ATT_ABSTRACT );

        int complexTypeBase      = fStringPool.addSymbol(
                                                        complexTypeDecl.getAttribute( SchemaSymbols.ATT_BASE ));
        String base = complexTypeDecl.getAttribute(SchemaSymbols.ATT_BASE);

        int complexTypeBlock     = fStringPool.addSymbol(
                                                        complexTypeDecl.getAttribute( SchemaSymbols.ATT_BLOCK ));
        String blockSet = complexTypeDecl.getAttribute( SchemaSymbols.ATT_BLOCK );

        int complexTypeContent   = fStringPool.addSymbol(
                                                        complexTypeDecl.getAttribute( SchemaSymbols.ATT_CONTENT ));
        String content = complexTypeDecl.getAttribute(SchemaSymbols.ATT_CONTENT);

        int complexTypeDerivedBy =  fStringPool.addSymbol(
                                                         complexTypeDecl.getAttribute( SchemaSymbols.ATT_DERIVEDBY ));
        String derivedBy = complexTypeDecl.getAttribute( SchemaSymbols.ATT_DERIVEDBY );

        int complexTypeFinal     =  fStringPool.addSymbol(
                                                         complexTypeDecl.getAttribute( SchemaSymbols.ATT_FINAL ));
        String finalSet = complexTypeDecl.getAttribute( SchemaSymbols.ATT_FINAL );

        int complexTypeID        = fStringPool.addSymbol(
                                                        complexTypeDecl.getAttribute( SchemaSymbols.ATTVAL_ID ));
        String typeId = complexTypeDecl.getAttribute( SchemaSymbols.ATTVAL_ID );

        int complexTypeName      =  fStringPool.addSymbol(
                                                         complexTypeDecl.getAttribute( SchemaSymbols.ATT_NAME ));
        String typeName = complexTypeDecl.getAttribute(SchemaSymbols.ATT_NAME); 



        if (typeName.equals("")) { // gensym a unique name
            //typeName = "http://www.apache.org/xml/xerces/internalType"+fTypeCount++;
            typeName = "#"+fAnonTypeCount++;
        }

        int scopeDefined = fScopeCount++;
        int previousScope = fCurrentScope;
        fCurrentScope = scopeDefined;

        Element child = null;
        int contentSpecType = 0;
        int csnType = 0;
        int left = -2;
        int right = -2;
        Vector uses = new Vector();
        ComplexTypeInfo baseTypeInfo = null;  //if base is a complexType;
        DatatypeValidator baseTypeValidator = null; //if base is a simple type or a complex type derived from a simpleType
        DatatypeValidator simpleTypeValidator = null;
        int baseTypeSymbol = -1;
        String fullBaseName = "";
        boolean baseIsSimpleSimple = false;
        boolean baseIsComplexSimple = false;
        boolean derivedByRestriction = true;
        boolean derivedByExtension = false;
        int baseContentSpecHandle = -1;
        Element baseTypeNode = null;


        //int parsedderivedBy = parseComplexDerivedBy(derivedBy);
        //handle the inhreitance here. 
        if (base.length()>0) {
            if (derivedBy.length() == 0) {
                reportGenericSchemaError("derivedBy must be present when base is present in " 
                                         +SchemaSymbols.ELT_COMPLEXTYPE
                                         +" "+ typeName);
            }
            else {
                if (derivedBy.equals(SchemaSymbols.ATTVAL_EXTENSION)) {
                    derivedByRestriction = false;
                }
                
                String prefix = "";
                String localpart = base;
                int colonptr = base.indexOf(":");
                if ( colonptr > 0) {
                    prefix = base.substring(0,colonptr);
                    localpart = base.substring(colonptr+1);
                }
                int localpartIndex = fStringPool.addSymbol(localpart);
                String typeURI = resolvePrefixToURI(prefix);
                if (!typeURI.equals(targetNSUriString)) {
                    baseTypeInfo = getTypeInfoFromNS(typeURI, localpart);
                    // REVISIT: baseTypeValidator = getTypeValidatorFromNS(typeURI, localpart);
                }
                else {
                
                    fullBaseName = typeURI+","+localpart;
                        // try to locate the base type first
                    baseTypeInfo = (ComplexTypeInfo) fComplexTypeRegistry.get(fullBaseName);
                    if (baseTypeInfo == null) {
                        baseTypeValidator = fDatatypeRegistry.getValidatorFor(fullBaseName);
                        if (baseTypeValidator == null) {
                            baseTypeNode = getTopLevelComponentByName(SchemaSymbols.ELT_COMPLEXTYPE,localpart);
                            if (baseTypeNode != null) {
                                baseTypeSymbol = traverseComplexTypeDecl( baseTypeNode );
                                baseTypeInfo = (ComplexTypeInfo)
                                fComplexTypeRegistry.get(fStringPool.toString(baseTypeSymbol)); //REVIST: should it be fullBaseName;
                            }
                            else {
                                baseTypeNode = getTopLevelComponentByName(SchemaSymbols.ELT_SIMPLETYPE, localpart);
                                if (baseTypeNode != null) {
                                    baseTypeSymbol = traverseSimpleTypeDecl( baseTypeNode );
                                    baseTypeValidator = fDatatypeRegistry.getValidatorFor(fullBaseName);
                                    baseIsSimpleSimple = true;
                                }
                                else {
                                    reportGenericSchemaError("Base type could not be found : " + base);
                                }
                            }
                        }
                        else {
                            baseIsSimpleSimple = true;
                        }

                        //Schema Spec : 5.11: Complex Type Definition Properties Correct : 2
                        if (baseIsSimpleSimple && derivedByRestriction) {
                            reportGenericSchemaError("base is a simpledType, can't derive by restriction in " + typeName); 
                        }

                        //if  the base is a complexType
                        if (baseTypeInfo != null ) {

                            //Schema Spec : 5.11: Derivation Valid ( Extension ) 1.1.1
                            //              5.11: Derivation Valid ( Restriction, Complex ) 1.2.1
                            if (derivedByRestriction) {
                                //REVISIT: check base Type's finalset does not include "restriction"
                            }
                            else {
                                //REVISIT: check base Type's finalset doest not include "extension"
                            }

                            if ( baseTypeInfo.contentSpecHandle > -1) {
                                if (derivedByRestriction) {
                                    //REVISIT: !!! really hairy staff to check the particle derivation OK in 5.10
                                    checkParticleDerivationOK(complexTypeDecl, baseTypeNode);
                                }
                                baseContentSpecHandle = baseTypeInfo.contentSpecHandle;
                            }
                            else if ( baseTypeInfo.datatypeValidator != null ) {
                                baseTypeValidator = baseTypeInfo.datatypeValidator;
                                baseIsComplexSimple = true;
                            }
                        }

                        //Schema Spec : 5.11: Derivation Valid ( Extension ) 1.1.1
                        if (baseIsComplexSimple && !derivedByRestriction ) {
                            reportGenericSchemaError("base is ComplexSimple, can't derive by extension in " + typeName);
                        }


                    }
                }
            }
        }

        // skip refinement and annotations
        child = null;
        for (child = XUtil.getFirstChildElement(complexTypeDecl);
             child != null && (child.getNodeName().equals(SchemaSymbols.ELT_MINEXCLUSIVE) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_MININCLUSIVE) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_MAXEXCLUSIVE) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_MAXINCLUSIVE) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_PRECISION) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_SCALE) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_LENGTH) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_MINLENGTH) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_MAXLENGTH) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_ENCODING) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_PERIOD) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_DURATION) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_ENUMERATION) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_PATTERN) ||
                               child.getNodeName().equals(SchemaSymbols.ELT_ANNOTATION));
             child = XUtil.getNextSiblingElement(child)) 
        {
            //REVISIT: SimpleType restriction handling
            //if (child.getNodeName().equals(SchemaSymbols.ELT_RESTRICTIONS))
                reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
                                  new Object [] { "Restrictions" });
        }

            // if content = textonly, base is a datatype
        if (content.equals(SchemaSymbols.ATTVAL_TEXTONLY)) {
            if (fDatatypeRegistry.getValidatorFor(base) == null) // must be datatype
                        reportSchemaError(SchemaMessageProvider.NotADatatype,
                                          new Object [] { base }); //REVISIT check forward refs
            //handle datatypes
            contentSpecType = fStringPool.addSymbol("DATATYPE");
            left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                 fStringPool.addSymbol(base),
                                                 -1, false);

        } 
        else {   
            contentSpecType = fStringPool.addSymbol("CHILDREN");
            csnType = XMLContentSpec.CONTENTSPECNODE_SEQ;
            boolean mixedContent = false;
            boolean elementContent = false;
            boolean textContent = false;
            left = -2;
            right = -2;
            boolean hadContent = false;

            if (content.equals(SchemaSymbols.ATTVAL_EMPTY)) {
                contentSpecType = fStringPool.addSymbol("EMPTY");
                left = -1; // no contentSpecNode needed
            } else if (content.equals(SchemaSymbols.ATTVAL_MIXED) ) {
                contentSpecType = fStringPool.addSymbol("MIXED");
                mixedContent = true;
                csnType = XMLContentSpec.CONTENTSPECNODE_CHOICE;
            } else if (content.equals(SchemaSymbols.ATTVAL_ELEMENTONLY) || content.equals("")) {
                elementContent = true;
            } else if (content.equals(SchemaSymbols.ATTVAL_TEXTONLY)) {
                textContent = true;
            }

            if (mixedContent) {
                // add #PCDATA leaf

                left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                     -1, // -1 means "#PCDATA" is name
                                                           -1, false);
                csnType = XMLContentSpec.CONTENTSPECNODE_CHOICE;
            }

            for (;
                 child != null;
                 child = XUtil.getNextSiblingElement(child)) {

                int index = -2;  // to save the particle's contentSpec handle 
                hadContent = true;

                boolean seeParticle = false;

                String childName = child.getNodeName();

                if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                    //if (child.getAttribute(SchemaSymbols.ATT_REF).equals("") ) {

                        if (mixedContent || elementContent) {
                            //REVISIT: unfinished
                            QName eltQName = traverseElementDecl(child);
                            index = fValidator.addContentSpecNode( XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                                   eltQName.localpart,
                                                                   eltQName.uri, 
                                                                   false);
                            seeParticle = true;

                        } 
                        else {
                            reportSchemaError(SchemaMessageProvider.EltRefOnlyInMixedElemOnly, null);
                        }

                    //} 
                    //else // REVISIT: do we really need this? or
                         // should it be done in the traverseElementDecl
                         // SchemaSymbols.ATT_REF != ""
                        //index = traverseElementRef(child);

                } 
                else if (childName.equals(SchemaSymbols.ELT_GROUP)) {
                    /* //if (elementContent) {
                        int groupNameIndex = 0;
                        if (child.getAttribute(SchemaSymbols.ATT_REF).equals("")) {
                            groupNameIndex = traverseGroup(child);
                        } else
                            groupNameIndex = traverseGroupRef(child);
                        index = getContentSpecHandleForElementType(groupNameIndex);
                    //} else if (!elementContent)
                        //reportSchemaError(SchemaMessageProvider.OnlyInEltContent,
                                //        new Object [] { "group" });
                     */
                    index = traverseGroupDecl(child);
                    seeParticle = true;
                  
                } 
                else if (childName.equals(SchemaSymbols.ELT_ALL)) {
                    index = traverseAll(child);
                    seeParticle = true;
                  
                } 
                else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                    index = traverseChoice(child);
                    seeParticle = true;
                  
                } 
                else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                    index = traverseSequence(child);
                    seeParticle = true;
                  
                } 
                else if (childName.equals(SchemaSymbols.ELT_ATTRIBUTE) ||
                           childName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                    break; // attr processing is done below
                } 
                else if (childName.equals(SchemaSymbols.ELT_ANY)) {
                    contentSpecType = fStringPool.addSymbol("ANY");
                    left = -1;
                } 
                else { // datatype qual   
                    if (base.equals(""))
                        reportSchemaError(SchemaMessageProvider.DatatypeWithType, null);
                    else
                        reportSchemaError(SchemaMessageProvider.DatatypeQualUnsupported,
                                          new Object [] { childName });
                }

                // check the minOccurs and maxOccurs of the particle, and fix the  
                // contentspec accordingly
                if (seeParticle) {
                    index = expandContentModel(index, child);

                } //end of if (seeParticle)

                uses.addElement(new Integer(index));
                if (left == -2) {
                    left = index;
                } else if (right == -2) {
                    right = index;
                } else {
                    left = fValidator.addContentSpecNode(csnType, left, right, false);
                    right = index;
                }
            } //end looping through the children

            if (hadContent && right != -2)
                left = fValidator.addContentSpecNode(csnType, left, right, false);

            if (mixedContent && hadContent) {
                // set occurrence count
                left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE,
                                                     left, -1, false);
            }
        }
        
        // if derived by extension and base complextype has a content model, 
        // compose the final content model by concatenating the base and the 
        // current in sequence.
        if (!derivedByRestriction && baseContentSpecHandle > -1 ) {
            left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ, 
                                                 baseContentSpecHandle,
                                                 left,
                                                 false);
        }
        // REVISIT: keep it here for now, stick in ElementDeclPool as a hack
        //int typeIndex = fValidator.addElementDecl(typeNameIndex, contentSpecType, left, false);

        if (!typeName.startsWith("#")) {
            typeName = targetNSUriString + "," + typeName;
        }
        ComplexTypeInfo typeInfo = new ComplexTypeInfo();
        typeInfo.base = base;
        typeInfo.derivedBy = parseComplexDerivedBy(complexTypeDecl.getAttribute(SchemaSymbols.ATT_DERIVEDBY));
        typeInfo.scopeDefined = scopeDefined; 
        typeInfo.contentSpecHandle = left;
        typeInfo.contentType = contentSpecType;
        typeInfo.datatypeValidator = simpleTypeValidator;
        typeInfo.blockSet = parseBlockSet(complexTypeDecl.getAttribute(SchemaSymbols.ATT_BLOCK));
        typeInfo.finalSet = parseFinalSet(complexTypeDecl.getAttribute(SchemaSymbols.ATT_FINAL));

        fComplexTypeRegistry.put(typeName,typeInfo);

        //for (int x = 0; x < uses.size(); x++)
            //addUse(typeNameIndex, (Integer)uses.elementAt(x));
        
        int typeNameIndex = fStringPool.addSymbol(typeName); //REVISIT namespace clashes possible


        // REVISIT: this part is definitely broken!!!
        // (attribute | attrGroupRef)*
        for (;
             child != null;
             child = XUtil.getNextSiblingElement(child)) {

            String childName = child.getNodeName();

            if (childName.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                traverseAttributeDecl(child, typeInfo);
            } 
            else if ( childName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP) ) { 

                traverseAttributeGroupDecl(child,typeInfo);
            }
        }

        if (baseIsComplexSimple) {
            //TO DO: add the attributes de
        }

        fCurrentScope = previousScope;

        return typeNameIndex;


    } // end of method: traverseComplexTypeDecl

    private void checkParticleDerivationOK(Element derivedTypeNode, Element baseTypeNode) {
    }

    private int expandContentModel ( int index, Element particle) throws Exception {
        
        String minOccurs = particle.getAttribute(SchemaSymbols.ATT_MINOCCURS);
        String maxOccurs = particle.getAttribute(SchemaSymbols.ATT_MINOCCURS);    

        int min=1, max=1;

        if (minOccurs.equals("")) {
            minOccurs = "1";
        }
        if (maxOccurs.equals("") ){
            if ( minOccurs.equals("0")) {
                maxOccurs = "1";
            }
            else {
                maxOccurs = minOccurs;
            }
        }


        int leafIndex = index;
        //REVISIT: !!! minoccurs, maxoccurs.
        if (minOccurs.equals("1")&& maxOccurs.equals("1")) {

        }
        else if (minOccurs.equals("0")&& maxOccurs.equals("1")) {
            //zero or one
            index = fValidator.addContentSpecNode( XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE,
                                                   index,
                                                   -1,
                                                   false);
        }
        else if (minOccurs.equals("0")&& maxOccurs.equals("unbounded")) {
            //zero or more
            index = fValidator.addContentSpecNode( XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE,
                                                   index,
                                                   -1,
                                                   false);
        }
        else if (minOccurs.equals("1")&& maxOccurs.equals("unbounded")) {
            //one or more
            index = fValidator.addContentSpecNode( XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE,
                                                   index,
                                                   -1,
                                                   false);
        }
        else if (maxOccurs.equals("unbounded") ) {
            // >=2 or more
            try {
                min = Integer.parseInt(minOccurs);
            }
            catch (Exception e) {
                //REVISIT; error handling
                e.printStackTrace();
            }
            if (min<2) {
                //REVISIT: report Error here
            }

            // => a,a,..,a+
            index = fValidator.addContentSpecNode( XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE,
                   index,
                   -1,
                   false);

            for (int i=0; i < (min-1); i++) {
                index = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                      index,
                                                      leafIndex,
                                                      false);
            }

        }
        else {
            // {n,m} => a,a,a,...(a),(a),...
            try {
                min = Integer.parseInt(minOccurs);
                max = Integer.parseInt(maxOccurs);
            }
            catch (Exception e){
                //REVISIT; error handling
                e.printStackTrace();
            }
            for (int i=0; i<(min-1); i++) {
                index = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                      index,
                                                      leafIndex,
                                                      false);

            }
            if (max>min ) {
                int optional = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE,
                                                             leafIndex,
                                                             -1,
                                                             false);
                for (int i=0; i < (max-min); i++) {
                    index = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                          index,
                                                          optional,
                                                          false);
                }
            }
        }

        return index;
    }

    /**
     * Traverses Schema attribute declaration.
     *   
     *       <attribute 
     *         form = qualified | unqualified 
     *         id = ID 
     *         name = NCName 
     *         ref = QName 
     *         type = QName 
     *         use = default | fixed | optional | prohibited | required 
     *         value = string>
     *         Content: (annotation? , simpleType?)
     *       <attribute/>
     * 
     * @param attributeDecl
     * @return 
     * @exception Exception
     */
    private int traverseAttributeDecl( Element attrDecl, ComplexTypeInfo typeInfo ) throws Exception {
        int attributeForm  =  fStringPool.addSymbol(
                                                   attrDecl.getAttribute( SchemaSymbols.ATT_FORM ));

        int attributeID    =  fStringPool.addSymbol(
                                                   attrDecl.getAttribute( SchemaSymbols.ATTVAL_ID ));

        int attributeName  =  fStringPool.addSymbol(
                                                   attrDecl.getAttribute( SchemaSymbols.ATT_NAME ));

        int attributeRef   =  fStringPool.addSymbol(
                                                   attrDecl.getAttribute( SchemaSymbols.ATT_REF ));

        int attributeType  =  fStringPool.addSymbol(
                                                   attrDecl.getAttribute( SchemaSymbols.ATT_TYPE ));

        int attributeUse   =  fStringPool.addSymbol(
                                                   attrDecl.getAttribute( SchemaSymbols.ATT_USE ));

        int attributeValue =  fStringPool.addSymbol(
                                                   attrDecl.getAttribute( SchemaSymbols.ATT_VALUE ));

        // attribute name
        int attName = fStringPool.addSymbol(attrDecl.getAttribute(SchemaSymbols.ATT_NAME));
        // form attribute
        String isQName = attrDecl.getAttribute(SchemaSymbols.ATT_EQUIVCLASS);

        // attribute type
        int attType = -1;
        int enumeration = -1;

        String ref = attrDecl.getAttribute(SchemaSymbols.ATT_REF); 
        String datatype = attrDecl.getAttribute(SchemaSymbols.ATT_TYPE);

        if (!ref.equals("")) {
            if (XUtil.getFirstChildElement(attrDecl) != null)
                reportSchemaError(SchemaMessageProvider.NoContentForRef, null);
            String prefix = "";
            String localpart = ref;
            int colonptr = ref.indexOf(":");
            if ( colonptr > 0) {
                prefix = ref.substring(0,colonptr);
                localpart = ref.substring(colonptr+1);
            }
            if (!resolvePrefixToURI(prefix).equals(targetNSUriString)) {
                // REVISIST: different NS, not supported yet.
                reportGenericSchemaError("Feature not supported: see an attribute from different NS");
            }
            Element referredAttribute = getTopLevelComponentByName(SchemaSymbols.ELT_ATTRIBUTE,localpart);
            if (referredAttribute != null) {
                traverseAttributeDecl(referredAttribute, typeInfo);
            }
            else {
                reportGenericSchemaError ( "Couldn't find top level attribute " + ref);
            }
            return -1;
        }

        if (datatype.equals("")) {
            Element child = XUtil.getFirstChildElement(attrDecl);
            while (child != null && !child.getNodeName().equals(SchemaSymbols.ELT_SIMPLETYPE))
                child = XUtil.getNextSiblingElement(child);
            if (child != null && child.getNodeName().equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                attType = fStringPool.addSymbol("DATATYPE");
                enumeration = traverseSimpleTypeDecl(child);
            } else 
                attType = fStringPool.addSymbol("CDATA");
        } else {
            if (datatype.equals("string")) {
                attType = fStringPool.addSymbol("CDATA");
            } else if (datatype.equals("ID")) {
                attType = fStringPool.addSymbol("ID");
            } else if (datatype.equals("IDREF")) {
                attType = fStringPool.addSymbol("IDREF");
            } else if (datatype.equals("IDREFS")) {
                attType = fStringPool.addSymbol("IDREFS");
            } else if (datatype.equals("ENTITY")) {
                attType = fStringPool.addSymbol("ENTITY");
            } else if (datatype.equals("ENTITIES")) {
                attType = fStringPool.addSymbol("ENTITIES");
            } else if (datatype.equals("NMTOKEN")) {
                Element e = XUtil.getFirstChildElement(attrDecl, "enumeration");
                if (e == null) {
                    attType = fStringPool.addSymbol("NMTOKEN");
                } else {
                    attType = fStringPool.addSymbol("ENUMERATION");
                    enumeration = fStringPool.startStringList();
                    for (Element literal = XUtil.getFirstChildElement(e, "literal");
                         literal != null;
                         literal = XUtil.getNextSiblingElement(literal, "literal")) {
                        int stringIndex = fStringPool.addSymbol(literal.getFirstChild().getNodeValue());
                        fStringPool.addStringToList(enumeration, stringIndex);
                    }
                    fStringPool.finishStringList(enumeration);
                }
            } else if (datatype.equals("NMTOKENS")) {
                attType = fStringPool.addSymbol("NMTOKENS");
            } else if (datatype.equals(SchemaSymbols.ELT_NOTATION)) {
                attType = fStringPool.addSymbol("NOTATION");
            } else { // REVISIT: Danger: assuming all other ATTR types are datatypes
                //REVISIT check against list of validators to ensure valid type name
                attType = fStringPool.addSymbol("DATATYPE");
                enumeration = fStringPool.addSymbol(datatype);
            }
        }

        // attribute default type
        int attDefaultType = -1;
        int attDefaultValue = -1;

        String use = attrDecl.getAttribute(SchemaSymbols.ATT_USE);
        boolean required = use.equals(SchemaSymbols.ATTVAL_REQUIRED);

        if (required) {
            attDefaultType = fStringPool.addSymbol("#REQUIRED");
        } else {
            if (use.equals(SchemaSymbols.ATTVAL_FIXED)) {
                String fixed = attrDecl.getAttribute(SchemaSymbols.ATT_VALUE);
                if (!fixed.equals("")) {
                    attDefaultType = fStringPool.addSymbol("#FIXED");
                    attDefaultValue = fStringPool.addString(fixed);
                } 
            }
            else if (use.equals(SchemaSymbols.ATTVAL_DEFAULT)) {
                // attribute default value
                String defaultValue = attrDecl.getAttribute(SchemaSymbols.ATT_VALUE);
                if (!defaultValue.equals("")) {
                    attDefaultType = fStringPool.addSymbol("");
                    attDefaultValue = fStringPool.addString(defaultValue);
                } 
            }
            else if (use.equals(SchemaSymbols.ATTVAL_PROHIBITED)) {
                attDefaultType = fStringPool.addSymbol("#PROHIBITED");
                attDefaultValue = fStringPool.addString("");
            }
            else {
                attDefaultType = fStringPool.addSymbol("#IMPLIED");
            }       // check default value is valid for the datatype.
            if (attType == fStringPool.addSymbol("DATATYPE") && attDefaultValue != -1) {
                try { // REVISIT - integrate w/ error handling
                    String type = fStringPool.toString(enumeration);
                    DatatypeValidator v = fDatatypeRegistry.getValidatorFor(type);
                    if (v != null)
                        v.validate(fStringPool.toString(attDefaultValue));
                    else
                        reportSchemaError(SchemaMessageProvider.NoValidatorFor,
                                          new Object [] { type });
                } catch (InvalidDatatypeValueException idve) {
                    reportSchemaError(SchemaMessageProvider.IncorrectDefaultType,
                                      new Object [] { attrDecl.getAttribute(SchemaSymbols.ATT_NAME), idve.getMessage() });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Internal error in attribute datatype validation");
                }
            }
        }

        int uriIndex = -1;
        if ( isQName.equals(SchemaSymbols.ATTVAL_QUALIFIED)||
             defaultQualified || isTopLevel(attrDecl) ) {
            uriIndex = targetNSURI;
        }

        QName attQName = new QName(-1,attName,attName,uriIndex);

        // add attribute to attr decl pool in fValidator, and get back the head
        int newhead = fValidator.addAttDef( typeInfo.attlistHead, 
                                                              attQName, attType, 
                                                              enumeration, attDefaultType, 
                                                              attDefaultValue, true);
        if (newhead > -1 ) {
            typeInfo.attlistHead = newhead;
        }
        return -1;
    } // end of method traverseAttribute

    /*
    * 
    * <attributeGroup 
    *   id = ID 
    *   name = NCName
    *   ref = QName>
    *   Content: (annotation?, (attribute|attributeGroup), anyAttribute?)
    * </>
    * 
    */
    private int traverseAttributeGroupDecl( Element attrGrpDecl, ComplexTypeInfo typeInfo ) throws Exception {
        // attribute name
        int attGrpName = fStringPool.addSymbol(attrGrpDecl.getAttribute(SchemaSymbols.ATT_NAME));
        
        String ref = attrGrpDecl.getAttribute(SchemaSymbols.ATT_REF); 

        // attribute type
        int attType = -1;
        int enumeration = -1;

        if (!ref.equals("")) {
            if (XUtil.getFirstChildElement(attrGrpDecl) != null)
                reportSchemaError(SchemaMessageProvider.NoContentForRef, null);
            String prefix = "";
            String localpart = ref;
            int colonptr = ref.indexOf(":");
            if ( colonptr > 0) {
                prefix = ref.substring(0,colonptr);
                localpart = ref.substring(colonptr+1);
            }
            if (!resolvePrefixToURI(prefix).equals(targetNSUriString)) {
                // REVISIST: different NS, not supported yet.
                reportGenericSchemaError("Feature not supported: see an attribute from different NS");
            }
            Element referredAttrGrp = getTopLevelComponentByName(SchemaSymbols.ELT_ATTRIBUTEGROUP,localpart);
            if (referredAttrGrp != null) {
                traverseAttributeDecl(referredAttrGrp, typeInfo);
            }
            else {
                reportGenericSchemaError ( "Couldn't find top level attributegroup " + ref);
            }
            return -1;
        }

        for ( Element child = XUtil.getFirstChildElement(attrGrpDecl); 
             child != null ; child = XUtil.getNextSiblingElement(child)) {
       
            if ( child.getNodeName().equals(SchemaSymbols.ELT_ATTRIBUTE) ){
                traverseAttributeDecl(child, typeInfo);
            }
            else if ( child.getNodeName().equals(SchemaSymbols.ELT_ATTRIBUTEGROUP) ) {
                traverseAttributeGroupDecl(child, typeInfo);
            }
            else if (child.getNodeName().equals(SchemaSymbols.ELT_ANNOTATION) ) {
                // REVISIT: what about appInfo
            }
        }
        return -1;
    } // end of method traverseAttributeGroup
    
    /*
    // REVISIT addAttDef a hack for TraverseSchema 
    public int addAttDef(int attlistHeadIndex, QName attributeDecl, 
                         int attType, int enumeration, 
                         int attDefaultType, int attDefaultValue, 
                         boolean whenRestriction) throws Exception {

        int attlistIndex = attlistHeadIndex;

        int dupID = -1;
        int dupNotation = -1;
        while (attlistIndex != -1) {
            int attrChunk = attlistIndex >> CHUNK_SHIFT;
            int attrIndex = attlistIndex & CHUNK_MASK;
            // REVISIT: Validation. Attributes are also tuples.
            if (fStringPool.equalNames(fAttName[attrChunk][attrIndex], attributeDecl.rawname)) {
                // REVISIT
                if ( ! whenRestriction) {
                    reportGenericSchemaError("Duplicate attributes in "+typeInfo.name);
                }
                return -1;
            }
            if (fValidating) {
                if (attType == fIDSymbol && fAttType[attrChunk][attrIndex] == fIDSymbol) {
                    dupID = fAttName[attrChunk][attrIndex];
                }
                if (attType == fNOTATIONSymbol && fAttType[attrChunk][attrIndex] == fNOTATIONSymbol) {
                    dupNotation = fAttName[attrChunk][attrIndex];
                }
            }
            attlistIndex = fNextAttDef[attrChunk][attrIndex];
        }
        if (fValidating) {
            //REVISIT
            if (dupID != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupID),
                                  fStringPool.toString(attributeDecl.rawname) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_ID_ATTRIBUTE,
                                           XMLMessages.VC_ONE_ID_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }
            if (dupNotation != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupNotation),
                                  fStringPool.toString(attributeDecl.rawname) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE,
                                           XMLMessages.VC_ONE_NOTATION_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }
        }
        //
        // save the fields
        //
        int chunk = fAttDefCount >> CHUNK_SHIFT;
        int index = fAttDefCount & CHUNK_MASK;
        ensureAttrCapacity(chunk);
        fAttName[chunk][index] = attributeDecl.rawname;
        fAttType[chunk][index] = attType;
        fAttValidator[chunk][index] = getValidatorForAttType(attType);
        fEnumeration[chunk][index] = enumeration;
        fAttDefaultType[chunk][index] = attDefaultType;
        fAttDefIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
        fAttValue[chunk][index] = attDefaultValue;
        //
        // add to the attr list for this element
        //
        int nextIndex = -1;
        if (attDefaultValue != -1) {
            nextIndex = fAttlistHead[elemChunk][elemIndex];
            fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1) {
                fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
            }
        } else {
            nextIndex = fAttlistTail[elemChunk][elemIndex];
            fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1) {
                fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            }
            else {
                fNextAttDef[nextIndex >> CHUNK_SHIFT][nextIndex & CHUNK_MASK] = fAttDefCount;
                nextIndex = -1;
            }
        }
        nextIndex = attlistHeadIndex;
        fNextAttDef[chunk][index] = nextIndex;
        
        //return fAttDefCount++;
        return fAttDefCount++;

    } // addAttDef(QName,QName,int,int,int,int,boolean):int
*/

    /**
     * Traverse element declaration:
     *  <element
     *         abstract = boolean
     *         block = #all or (possibly empty) subset of {equivClass, extension, restriction}
     *         default = string
     *         equivClass = QName
     *         final = #all or (possibly empty) subset of {extension, restriction}
     *         fixed = string
     *         form = qualified | unqualified
     *         id = ID
     *         maxOccurs = string
     *         minOccurs = nonNegativeInteger
     *         name = NCName
     *         nullable = boolean
     *         ref = QName
     *         type = QName>
     *   Content: (annotation? , (simpleType | complexType)? , (unique | key | keyref)*)
     *   </element>
     * 
     * 
     *       The following are identity-constraint definitions
     *        <unique 
     *         id = ID 
     *         name = NCName>
     *         Content: (annotation? , (selector , field+))
     *       </unique>
     *       
     *       <key 
     *         id = ID 
     *         name = NCName>
     *         Content: (annotation? , (selector , field+))
     *       </key>
     *       
     *       <keyref 
     *         id = ID 
     *         name = NCName 
     *         refer = QName>
     *         Content: (annotation? , (selector , field+))
     *       </keyref>
     *       
     *       <selector>
     *         Content: XPathExprApprox : An XPath expression 
     *       </selector>
     *       
     *       <field>
     *         Content: XPathExprApprox : An XPath expression 
     *       </field>
     *       
     * 
     * @param elementDecl
     * @return 
     * @exception Exception
     */
    private QName traverseElementDecl(Element elementDecl) throws Exception {
        int elementBlock      =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_BLOCK ) );

        int elementDefault    =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_DEFAULT ));

        int elementEquivClass =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_EQUIVCLASS ));

        int elementFinal      =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_FINAL ));

        int elementFixed      =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_FIXED ));

        int elementForm       =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_FORM ));

        int elementID          =  fStringPool.addSymbol(
                                                       elementDecl.getAttribute( SchemaSymbols.ATTVAL_ID ));

        int elementMaxOccurs   =  fStringPool.addSymbol(
                                                       elementDecl.getAttribute( SchemaSymbols.ATT_MAXOCCURS ));

        int elementMinOccurs  =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_MINOCCURS ));

        int elemenName        =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_NAME ));

        int elementNullable   =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_NULLABLE ));

        int elementRef        =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_REF ));

        int elementType       =  fStringPool.addSymbol(
                                                      elementDecl.getAttribute( SchemaSymbols.ATT_TYPE ));

        int contentSpecType      = -1;
        int contentSpecNodeIndex = -1;
        int typeNameIndex = -1;
        int scopeDefined = -1; //signal a error if -1 gets gets through 
                                //cause scope can never be -1.

        String name = elementDecl.getAttribute(SchemaSymbols.ATT_NAME);
        String ref = elementDecl.getAttribute(SchemaSymbols.ATT_REF);
        String type = elementDecl.getAttribute(SchemaSymbols.ATT_TYPE);
        String minOccurs = elementDecl.getAttribute(SchemaSymbols.ATT_MINOCCURS);
        String maxOccurs = elementDecl.getAttribute(SchemaSymbols.ATT_MAXOCCURS);
        String dflt = elementDecl.getAttribute(SchemaSymbols.ATT_DEFAULT);
        String fixed = elementDecl.getAttribute(SchemaSymbols.ATT_FIXED);
        String equivClass = elementDecl.getAttribute(SchemaSymbols.ATT_EQUIVCLASS);
        // form attribute
        String isQName = elementDecl.getAttribute(SchemaSymbols.ATT_EQUIVCLASS);

        if (isTopLevel(elementDecl)) {
        
            int nameIndex = fStringPool.addSymbol(name);
            int eltKey = fValidator.getDeclaration(nameIndex,0);
            if (eltKey > -1 ) {
                return new QName(-1,nameIndex,nameIndex,targetNSURI);
            }
        }
        int attrCount = 0;
        if (!ref.equals("")) attrCount++;
        if (!type.equals("")) attrCount++;
                //REVISIT top level check for ref & archref
        if (attrCount > 1)
            reportSchemaError(SchemaMessageProvider.OneOfTypeRefArchRef, null);

        if (!ref.equals("")) {
            if (XUtil.getFirstChildElement(elementDecl) != null)
                reportSchemaError(SchemaMessageProvider.NoContentForRef, null);
            String prefix = "";
            String localpart = ref;
            int colonptr = ref.indexOf(":");
            if ( colonptr > 0) {
                prefix = ref.substring(0,colonptr);
                localpart = ref.substring(colonptr+1);
            }
            int localpartIndex = fStringPool.addSymbol(localpart);
            QName eltName = new QName(  fStringPool.addSymbol(prefix),
                                      localpartIndex,
                                      fStringPool.addSymbol(ref),
                                      fStringPool.addSymbol(resolvePrefixToURI(prefix)) );
            int elementIndex = fValidator.getDeclaration(localpartIndex, 0);
            //if not found, traverse the top level element that if referenced
            if (elementIndex == -1 ) {
                eltName= 
                    traverseElementDecl(
                        getTopLevelComponentByName(SchemaSymbols.ELT_ELEMENT,localpart)
                        );
            }
            return eltName;
        }
                
        
        ComplexTypeInfo typeInfo = new ComplexTypeInfo();

        // element has a single child element, either a datatype or a type, null if primitive
        Element content = XUtil.getFirstChildElement(elementDecl);
        
        while (content != null && content.getNodeName().equals(SchemaSymbols.ELT_ANNOTATION))
            content = XUtil.getNextSiblingElement(content);
        
        boolean typeSet = false;

        if (content != null) {
            
            String contentName = content.getNodeName();
            
            if (contentName.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                
                typeNameIndex = traverseComplexTypeDecl(content);
                typeInfo = (ComplexTypeInfo)
                    fComplexTypeRegistry.get(fStringPool.toString(typeNameIndex));

                contentSpecNodeIndex = typeInfo.contentSpecHandle;
                contentSpecType = typeInfo.contentType;
                scopeDefined = typeInfo.scopeDefined;

                typeSet = true;

            } 
            else if (contentName.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                //REVISIT: TO-DO, contenttype and simpletypevalidator.
                
                traverseSimpleTypeDecl(content);
                typeSet = true;
                reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
                                  new Object [] { "Nesting datatype declarations" });
                // contentSpecNodeIndex = traverseDatatypeDecl(content);
                // contentSpecType = fStringPool.addSymbol("DATATYPE");
            } else if (type.equals("")) { // "ur-typed" leaf
                contentSpecType = fStringPool.addSymbol("UR_TYPE");
                // set occurrence count
                contentSpecNodeIndex = -1;
            } else {
                System.out.println("unhandled case in TraverseElementDecl");
            }
        } 
        if (typeSet && (type.length()>0)) {
            reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
                              new Object [] { "can have type when have a annoymous type" });
        }
        else if (!type.equals("")) { // type specified as an attribute, 
            String prefix = "";
            String localpart = type;
            int colonptr = ref.indexOf(":");
            if ( colonptr > 0) {
                prefix = type.substring(0,colonptr);
                localpart = type.substring(colonptr+1);
            }
            String typeURI = resolvePrefixToURI(prefix);
            if (!typeURI.equals(targetNSUriString)) {
                typeInfo = getTypeInfoFromNS(typeURI, localpart);
            }
            typeInfo = (ComplexTypeInfo) fComplexTypeRegistry.get(typeURI+","+localpart);
            if (typeInfo == null) {
                DatatypeValidator dv = fDatatypeRegistry.getValidatorFor(typeURI+","+localpart);
                if (dv == null) {
                    Element topleveltype = getTopLevelComponentByName(SchemaSymbols.ELT_COMPLEXTYPE,localpart);
                    if (topleveltype != null) {
                        typeNameIndex = traverseComplexTypeDecl( topleveltype );
                        typeInfo = (ComplexTypeInfo)
                            fComplexTypeRegistry.get(fStringPool.toString(typeNameIndex));
                    }
                    else {
                        topleveltype = getTopLevelComponentByName(SchemaSymbols.ELT_SIMPLETYPE, localpart);
                        if (topleveltype != null) {
                            typeNameIndex = traverseSimpleTypeDecl( topleveltype );
                            dv = fDatatypeRegistry.getValidatorFor(typeURI+","+localpart);
                            // REVISIT ??do somthing for Simple type here:
                        }
                        else {
                            reportGenericSchemaError("type not found : " + localpart);
                        }

                    }

                }
            }
            if (typeInfo!=null) {
                contentSpecNodeIndex = typeInfo.contentSpecHandle;
                contentSpecType = typeInfo.contentType;
                scopeDefined = typeInfo.scopeDefined;
            }
   
        } // end of method traverseElementDecl
 
        //
        // Create element decl
        //

        int elementNameIndex     = fStringPool.addSymbol(elementDecl.getAttribute(SchemaSymbols.ATT_NAME));
        int localpartIndex = elementNameIndex;
        int uriIndex = -1;
        int enclosingScope = fCurrentScope;

        if ( isQName.equals(SchemaSymbols.ATTVAL_QUALIFIED)||
             defaultQualified || isTopLevel(elementDecl) ) {
            uriIndex = targetNSURI;
            enclosingScope = 0;
        }

        QName eltQName = new QName(-1,localpartIndex,elementNameIndex,uriIndex);
        // add element decl to pool
        fValidator.setCurrentScope(enclosingScope);
        int elementIndex = fValidator.addElementDecl(eltQName, scopeDefined, contentSpecType, contentSpecNodeIndex, true);
        //        System.out.println("elementIndex:"+elementIndex+" "+elementDecl.getAttribute(ATT_NAME)+" eltType:"+elementName+" contentSpecType:"+contentSpecType+
        //                           " SpecNodeIndex:"+ contentSpecNodeIndex);

        // copy up attribute decls from type object
        if (typeInfo != null) {
            fValidator.setCurrentScope(enclosingScope);
            fValidator.copyAttsForSchema(typeInfo.attlistHead, eltQName);
        }
        else {
            // REVISIT: should we report error from here?
        }

        return eltQName;

    }

    Element getTopLevelComponentByName(String componentCategory, String name) throws Exception {
        Element child = XUtil.getFirstChildElement(fSchemaRootElement);

        if (child == null) {
            return null;
        }

        while (child != null ){
            if ( child.getNodeName().equals(componentCategory)) {
                if (child.getAttribute(SchemaSymbols.ATT_NAME).equals(name)) {
                    return child;
                }
            }
            child = XUtil.getNextSiblingElement(child);
        }

        return null;
    }

    boolean isTopLevel(Element component) {
        if (component.getParentNode() == fSchemaRootElement ) {
            return true;
        }
        return false;
    }
    
    ComplexTypeInfo getTypeInfoFromNS(String typeURI, String localpart){
        return null;
    }
    /**
     * Traverse attributeGroup Declaration
     * 
     *   <attributeGroup
     *         id = ID
     *         ref = QName>
     *         Content: (annotation?)
     *      </>
     * 
     * @param elementDecl
     * @exception Exception
     */
    /*private int traverseAttributeGroupDecl( Element attributeGroupDecl ) throws Exception {
        int attributeGroupID         =  fStringPool.addSymbol(
                                                             attributeGroupDecl.getAttribute( SchemaSymbols.ATTVAL_ID ));

        int attributeGroupName      =  fStringPool.addSymbol(
                                                            attributeGroupDecl.getAttribute( SchemaSymbols.ATT_NAME ));

        return -1;
    }*/


    /**
     * Traverse Group Declaration.
     * 
     * <group 
     *         id = ID 
     *         maxOccurs = string 
     *         minOccurs = nonNegativeInteger 
     *         name = NCName 
     *         ref = QName>
     *   Content: (annotation? , (element | group | all | choice | sequence | any)*)
     * <group/>
     * 
     * @param elementDecl
     * @return 
     * @exception Exception
     */
    private int traverseGroupDecl( Element groupDecl ) throws Exception {
        int groupID         =  fStringPool.addSymbol(
            groupDecl.getAttribute( SchemaSymbols.ATTVAL_ID ));

        int groupMaxOccurs  =  fStringPool.addSymbol(
            groupDecl.getAttribute( SchemaSymbols.ATT_MAXOCCURS ));
        int groupMinOccurs  =  fStringPool.addSymbol(
            groupDecl.getAttribute( SchemaSymbols.ATT_MINOCCURS ));

        //int groupName      =  fStringPool.addSymbol(
            //groupDecl.getAttribute( SchemaSymbols.ATT_NAME ));

        int grouRef        =  fStringPool.addSymbol(
            groupDecl.getAttribute( SchemaSymbols.ATT_REF ));

        String groupName = groupDecl.getAttribute(SchemaSymbols.ATT_NAME);
        String ref = groupDecl.getAttribute(SchemaSymbols.ATT_REF);

        if (!ref.equals("")) {
            if (XUtil.getFirstChildElement(groupDecl) != null)
                reportSchemaError(SchemaMessageProvider.NoContentForRef, null);
            String prefix = "";
            String localpart = ref;
            int colonptr = ref.indexOf(":");
            if ( colonptr > 0) {
                prefix = ref.substring(0,colonptr);
                localpart = ref.substring(colonptr+1);
            }
            int localpartIndex = fStringPool.addSymbol(localpart);
            int contentSpecIndex = 
                traverseGroupDecl(
                    getTopLevelComponentByName(SchemaSymbols.ELT_GROUP,localpart)
                    );
            
            return contentSpecIndex;
        }

        boolean traverseElt = true; 
        if (fCurrentScope == 0) {
            traverseElt = false;
        }

        Element child = XUtil.getFirstChildElement(groupDecl);
        while (child != null && child.getNodeName().equals(SchemaSymbols.ELT_ANNOTATION))
            child = XUtil.getNextSiblingElement(child);

        int contentSpecType = 0;
        int csnType = 0;
        int allChildren[] = null;
        int allChildCount = 0;

        csnType = XMLContentSpec.CONTENTSPECNODE_SEQ;
        contentSpecType = fStringPool.addSymbol("CHILDREN");
        
        int left = -2;
        int right = -2;
        boolean hadContent = false;

        for (;
             child != null;
             child = XUtil.getNextSiblingElement(child)) {
            int index = -2;
            hadContent = true;

            boolean seeParticle = false;
            String childName = child.getNodeName();
            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                QName eltQName = traverseElementDecl(child);
                index = fValidator.addContentSpecNode( XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                       eltQName.localpart,
                                                       eltQName.uri, 
                                                       false);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_GROUP)) {
                index = traverseGroupDecl(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_ALL)) {
                index = traverseAll(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                index = traverseChoice(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                index = traverseSequence(child);
                seeParticle = true;

            } 
            else {
                reportSchemaError(SchemaMessageProvider.GroupContentRestricted,
                                  new Object [] { "group", childName });
            }

            if (seeParticle) {
                index = expandContentModel( index, child);
            }
            if (left == -2) {
                left = index;
            } else if (right == -2) {
                right = index;
            } else {
                left = fValidator.addContentSpecNode(csnType, left, right, false);
                right = index;
            }
        }
        if (hadContent && right != -2)
            left = fValidator.addContentSpecNode(csnType, left, right, false);


        return left;
    }
    
    /**
    *
    * Traverse the Sequence declaration
    * 
    * <sequence 
    *   id = ID 
    *   maxOccurs = string 
    *   minOccurs = nonNegativeInteger>
    *   Content: (annotation? , (element | group | choice | sequence | any)*)
    * </sequence>
    * 
    **/
    int traverseSequence (Element sequenceDecl) throws Exception {
            
        Element child = XUtil.getFirstChildElement(sequenceDecl);
        while (child != null && child.getNodeName().equals(SchemaSymbols.ELT_ANNOTATION))
            child = XUtil.getNextSiblingElement(child);

        int contentSpecType = 0;
        int csnType = 0;

        csnType = XMLContentSpec.CONTENTSPECNODE_SEQ;
        contentSpecType = fStringPool.addSymbol("CHILDREN");

        int left = -2;
        int right = -2;
        boolean hadContent = false;

        for (;
             child != null;
             child = XUtil.getNextSiblingElement(child)) {
            int index = -2;
            hadContent = true;

            boolean seeParticle = false;
            String childName = child.getNodeName();
            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                QName eltQName = traverseElementDecl(child);
                index = fValidator.addContentSpecNode( XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                       eltQName.localpart,
                                                       eltQName.uri, 
                                                       false);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_GROUP)) {
                index = traverseGroupDecl(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_ALL)) {
                index = traverseAll(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                index = traverseChoice(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                index = traverseSequence(child);
                seeParticle = true;

            } 
            else {
                reportSchemaError(SchemaMessageProvider.GroupContentRestricted,
                                  new Object [] { "group", childName });
            }

            if (seeParticle) {
                index = expandContentModel( index, child);
            }
            if (left == -2) {
                left = index;
            } else if (right == -2) {
                right = index;
            } else {
                left = fValidator.addContentSpecNode(csnType, left, right, false);
                right = index;
            }
        }

        if (hadContent && right != -2)
            left = fValidator.addContentSpecNode(csnType, left, right, false);

        return left;
    }
    
    /**
    *
    * Traverse the Sequence declaration
    * 
    * <choice
    *   id = ID 
    *   maxOccurs = string 
    *   minOccurs = nonNegativeInteger>
    *   Content: (annotation? , (element | group | choice | sequence | any)*)
    * </choice>
    * 
    **/
    int traverseChoice (Element choiceDecl) throws Exception {
            
        // REVISIT: traverseChoice, traverseSequence can be combined
        Element child = XUtil.getFirstChildElement(choiceDecl);
        while (child != null && child.getNodeName().equals(SchemaSymbols.ELT_ANNOTATION))
            child = XUtil.getNextSiblingElement(child);

        int contentSpecType = 0;
        int csnType = 0;

        csnType = XMLContentSpec.CONTENTSPECNODE_CHOICE;
        contentSpecType = fStringPool.addSymbol("CHILDREN");

        int left = -2;
        int right = -2;
        boolean hadContent = false;

        for (;
             child != null;
             child = XUtil.getNextSiblingElement(child)) {
            int index = -2;
            hadContent = true;

            boolean seeParticle = false;
            String childName = child.getNodeName();
            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                QName eltQName = traverseElementDecl(child);
                index = fValidator.addContentSpecNode( XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                       eltQName.localpart,
                                                       eltQName.uri, 
                                                       false);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_GROUP)) {
                index = traverseGroupDecl(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_ALL)) {
                index = traverseAll(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                index = traverseChoice(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                index = traverseSequence(child);
                seeParticle = true;

            } 
            else {
                reportSchemaError(SchemaMessageProvider.GroupContentRestricted,
                                  new Object [] { "group", childName });
            }

            if (seeParticle) {
                index = expandContentModel( index, child);
            }
            if (left == -2) {
                left = index;
            } else if (right == -2) {
                right = index;
            } else {
                left = fValidator.addContentSpecNode(csnType, left, right, false);
                right = index;
            }
        }

        if (hadContent && right != -2)
            left = fValidator.addContentSpecNode(csnType, left, right, false);

        return left;
    }
    

   /**
    * 
    * Traverse the "All" declaration
    *
    * <all 
    *   id = ID 
    *   maxOccurs = string 
    *   minOccurs = nonNegativeInteger>
    *   Content: (annotation? , (element | group | choice | sequence | any)*)
    * </all>
    *   
    **/

    int traverseAll( Element allDecl) throws Exception {


        Element child = XUtil.getFirstChildElement(allDecl);

        while (child != null && child.getNodeName().equals(SchemaSymbols.ELT_ANNOTATION))
            child = XUtil.getNextSiblingElement(child);

        int allChildren[] = null;
        int allChildCount = 0;

        int left = -2;

        for (;
             child != null;
             child = XUtil.getNextSiblingElement(child)) {

            int index = -2;
            boolean seeParticle = false;

            String childName = child.getNodeName();

            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                QName eltQName = traverseElementDecl(child);
                index = fValidator.addContentSpecNode( XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                       eltQName.localpart,
                                                       eltQName.uri, 
                                                       false);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_GROUP)) {
                index = traverseGroupDecl(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_ALL)) {
                index = traverseAll(child);
                seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                    index = traverseChoice(child);
                    seeParticle = true;

            } 
            else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                index = traverseSequence(child);
                seeParticle = true;

            } 
            else {
                reportSchemaError(SchemaMessageProvider.GroupContentRestricted,
                                  new Object [] { "group", childName });
            }

            if (seeParticle) {
                index = expandContentModel( index, child);
            }
            allChildren[allChildCount++] = index;
        }

        left = buildAllModel(allChildren,allChildCount);

        return left;
    }
    
    /** builds the all content model */
    private int buildAllModel(int children[], int count) throws Exception {

        // build all model
        if (count > 1) {

            // create and initialize singletons
            XMLContentSpec.Node choice = new XMLContentSpec.Node();

            choice.type = XMLContentSpec.CONTENTSPECNODE_CHOICE;
            choice.value = -1;
            choice.otherValue = -1;

            // build all model
            sort(children, 0, count);
            int index = buildAllModel(children, 0, choice);

            return index;
        }

        if (count > 0) {
            return children[0];
        }

        return -1;
    }

    /** Builds the all model. */
    private int buildAllModel(int src[], int offset,
                              XMLContentSpec.Node choice) throws Exception {

        // swap last two places
        if (src.length - offset == 2) {
            int seqIndex = createSeq(src);
            if (choice.value == -1) {
                choice.value = seqIndex;
            }
            else {
                if (choice.otherValue != -1) {
                    choice.value = fValidator.addContentSpecNode(choice.type, choice.value, choice.otherValue, false);
                }
                choice.otherValue = seqIndex;
            }
            swap(src, offset, offset + 1);
            seqIndex = createSeq(src);
            if (choice.value == -1) {
                choice.value = seqIndex;
            }
            else {
                if (choice.otherValue != -1) {
                    choice.value = fValidator.addContentSpecNode(choice.type, choice.value, choice.otherValue, false);
                }
                choice.otherValue = seqIndex;
            }
            return fValidator.addContentSpecNode(choice.type, choice.value, choice.otherValue, false);
        }

        // recurse
        for (int i = offset; i < src.length - 1; i++) {
            choice.value = buildAllModel(src, offset + 1, choice);
            choice.otherValue = -1;
            sort(src, offset, src.length - offset);
            shift(src, offset, i + 1);
        }

        int choiceIndex = buildAllModel(src, offset + 1, choice);
        sort(src, offset, src.length - offset);

        return choiceIndex;

    } // buildAllModel(int[],int,ContentSpecNode,ContentSpecNode):int

    /** Creates a sequence. */
    private int createSeq(int src[]) throws Exception {

        int left = src[0];
        int right = src[1];

        for (int i = 2; i < src.length; i++) {
            left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                       left, right, false);
            right = src[i];
        }

        return fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                   left, right, false);

    } // createSeq(int[]):int

    /** Shifts a value into position. */
    private void shift(int src[], int pos, int offset) {

        int temp = src[offset];
        for (int i = offset; i > pos; i--) {
            src[i] = src[i - 1];
        }
        src[pos] = temp;

    } // shift(int[],int,int)

    /** Simple sort. */
    private void sort(int src[], final int offset, final int length) {

        for (int i = offset; i < offset + length - 1; i++) {
            int lowest = i;
            for (int j = i + 1; j < offset + length; j++) {
                if (src[j] < src[lowest]) {
                    lowest = j;
                }
            }
            if (lowest != i) {
                int temp = src[i];
                src[i] = src[lowest];
                src[lowest] = temp;
            }
        }

    } // sort(int[],int,int)

    /** Swaps two values. */
    private void swap(int src[], int i, int j) {

        int temp = src[i];
        src[i] = src[j];
        src[j] = temp;

    } // swap(int[],int,int)

    /**
     * Traverse Wildcard declaration
     * 
     * <any 
     *   id = ID 
     *   maxOccurs = string 
     *   minOccurs = nonNegativeInteger 
     *   namespace = ##any | ##other | ##local | list of {uri, ##targetNamespace} 
     *   processContents = lax | skip | strict>
     *   Content: (annotation?)
     * </any>
     * @param elementDecl
     * @return 
     * @exception Exception
     */
    private int traverseWildcardDecl( Element wildcardDecl ) throws Exception {
        int wildcardID         =  fStringPool.addSymbol(
                                                       wildcardDecl.getAttribute( SchemaSymbols.ATTVAL_ID ));

        int wildcardMaxOccurs  =  fStringPool.addSymbol(
                                                       wildcardDecl.getAttribute( SchemaSymbols.ATT_MAXOCCURS ));

        int wildcardMinOccurs  =  fStringPool.addSymbol(
                                                       wildcardDecl.getAttribute( SchemaSymbols.ATT_MINOCCURS ));

        int wildcardNamespace  =  fStringPool.addSymbol(
                                                       wildcardDecl.getAttribute( SchemaSymbols.ATT_NAMESPACE ));

        int wildcardProcessContents =  fStringPool.addSymbol(
                                                            wildcardDecl.getAttribute( SchemaSymbols.ATT_PROCESSCONTENTS ));


        int wildcardContent =  fStringPool.addSymbol(
                                                    wildcardDecl.getAttribute( SchemaSymbols.ATT_CONTENT ));


        return -1;
    }
    
    
    private String resolvePrefixToURI(String prefix) {
        return "";
    }

    // utilities from Tom Watson's SchemaParser class
    // TODO: Need to make this more conformant with Schema int type parsing

    private int parseInt (String intString) throws Exception
    {
            if ( intString.equals("*") ) {
                    return Schema.INFINITY;
            } else {
                    return Integer.parseInt (intString);
            }
    }

    private int parseSimpleDerivedBy (String derivedByString) throws Exception
    {
            if ( derivedByString.equals (Schema.VAL_LIST) ) {
                    return Schema.LIST;
            } else if ( derivedByString.equals (Schema.VAL_RESTRICTION) ) {
                    return Schema.RESTRICTION;
            } else if ( derivedByString.equals (Schema.VAL_REPRODUCTION) ) {
                    return Schema.REPRODUCTION;
            } else {
                    reportGenericSchemaError ("Invalid value for 'derivedBy'");
                    return -1;
            }
    }

    private int parseComplexDerivedBy (String derivedByString)  throws Exception
    {
            if ( derivedByString.equals (Schema.VAL_EXTENSION) ) {
                    return Schema.EXTENSION;
            } else if ( derivedByString.equals (Schema.VAL_RESTRICTION) ) {
                    return Schema.RESTRICTION;
            } else if ( derivedByString.equals (Schema.VAL_REPRODUCTION) ) {
                    return Schema.REPRODUCTION;
            } else {
                    reportGenericSchemaError ( "Invalid value for 'derivedBy'" );
                    return -1;
            }
    }

    private int parseSimpleFinal (String finalString) throws Exception
    {
            if ( finalString.equals (Schema.VAL_POUNDALL) ) {
                    return Schema.ENUMERATION+Schema.RESTRICTION+Schema.LIST+Schema.REPRODUCTION;
            } else {
                    int enumerate = 0;
                    int restrict = 0;
                    int list = 0;
                    int reproduce = 0;

                    StringTokenizer t = new StringTokenizer (finalString, " ");
                    while (t.hasMoreTokens()) {
                            String token = t.nextToken ();

                            if ( token.equals (Schema.VAL_ENUMERATION) ) {
                                    if ( enumerate == 0 ) {
                                            enumerate = Schema.ENUMERATION;
                                    } else {
                                            reportGenericSchemaError ("enumeration in set twice");
                                    }
                            } else if ( token.equals (Schema.VAL_RESTRICTION) ) {
                                    if ( restrict == 0 ) {
                                            restrict = Schema.RESTRICTION;
                                    } else {
                                            reportGenericSchemaError ("restriction in set twice");
                                    }
                            } else if ( token.equals (Schema.VAL_LIST) ) {
                                    if ( list == 0 ) {
                                            list = Schema.LIST;
                                    } else {
                                            reportGenericSchemaError ("list in set twice");
                                    }
                            } else if ( token.equals (Schema.VAL_REPRODUCTION) ) {
                                    if ( reproduce == 0 ) {
                                            reproduce = Schema.REPRODUCTION;
                                    } else {
                                            reportGenericSchemaError ("reproduction in set twice");
                                    }
                            } else {
                                            reportGenericSchemaError (  "Invalid value (" + 
                                                                                                    finalString +
                                                                                                    ")" );
                            }
                    }

                    return enumerate+restrict+list+reproduce;
            }
    }

    private int parseComplexContent (String contentString)  throws Exception
    {
            if ( contentString.equals (Schema.VAL_EMPTY) ) {
                    return Schema.EMPTY;
            } else if ( contentString.equals (Schema.VAL_ELEMENTONLY) ) {
                    return Schema.ELEMENT_ONLY;
            } else if ( contentString.equals (Schema.VAL_TEXTONLY) ) {
                    return Schema.TEXT_ONLY;
            } else if ( contentString.equals (Schema.VAL_MIXED) ) {
                    return Schema.MIXED;
            } else {
                    reportGenericSchemaError ( "Invalid value for content" );
                    return -1;
            }
    }

    private int parseDerivationSet (String finalString)  throws Exception
    {
            if ( finalString.equals ("#all") ) {
                    return Schema.EXTENSION+Schema.RESTRICTION+Schema.REPRODUCTION;
            } else {
                    int extend = 0;
                    int restrict = 0;
                    int reproduce = 0;

                    StringTokenizer t = new StringTokenizer (finalString, " ");
                    while (t.hasMoreTokens()) {
                            String token = t.nextToken ();

                            if ( token.equals (Schema.VAL_EXTENSION) ) {
                                    if ( extend == 0 ) {
                                            extend = Schema.EXTENSION;
                                    } else {
                                            reportGenericSchemaError ( "extension already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_RESTRICTION) ) {
                                    if ( restrict == 0 ) {
                                            restrict = Schema.RESTRICTION;
                                    } else {
                                            reportGenericSchemaError ( "restriction already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_REPRODUCTION) ) {
                                    if ( reproduce == 0 ) {
                                            reproduce = Schema.REPRODUCTION;
                                    } else {
                                            reportGenericSchemaError ( "reproduction already in set" );
                                    }
                            } else {
                                    reportGenericSchemaError ( "Invalid final value (" + finalString + ")" );
                            }
                    }

                    return extend+restrict+reproduce;
            }
    }

    private int parseBlockSet (String finalString)  throws Exception
    {
            if ( finalString.equals ("#all") ) {
                    return Schema.EQUIVCLASS+Schema.EXTENSION+Schema.LIST+Schema.RESTRICTION+Schema.REPRODUCTION;
            } else {
                    int extend = 0;
                    int restrict = 0;
                    int reproduce = 0;

                    StringTokenizer t = new StringTokenizer (finalString, " ");
                    while (t.hasMoreTokens()) {
                            String token = t.nextToken ();

                            if ( token.equals (Schema.VAL_EQUIVCLASS) ) {
                                    if ( extend == 0 ) {
                                            extend = Schema.EQUIVCLASS;
                                    } else {
                                            reportGenericSchemaError ( "'equivClass' already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_EXTENSION) ) {
                                    if ( extend == 0 ) {
                                            extend = Schema.EXTENSION;
                                    } else {
                                            reportGenericSchemaError ( "extension already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_LIST) ) {
                                    if ( extend == 0 ) {
                                            extend = Schema.LIST;
                                    } else {
                                            reportGenericSchemaError ( "'list' already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_RESTRICTION) ) {
                                    if ( restrict == 0 ) {
                                            restrict = Schema.RESTRICTION;
                                    } else {
                                            reportGenericSchemaError ( "restriction already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_REPRODUCTION) ) {
                                    if ( reproduce == 0 ) {
                                            reproduce = Schema.REPRODUCTION;
                                    } else {
                                            reportGenericSchemaError ( "reproduction already in set" );
                                    }
                            } else {
                                    reportGenericSchemaError ( "Invalid final value (" + finalString + ")" );
                            }
                    }

                    return extend+restrict+reproduce;
            }
    }

    private int parseFinalSet (String finalString)  throws Exception
    {
            if ( finalString.equals ("#all") ) {
                    return Schema.EQUIVCLASS+Schema.EXTENSION+Schema.LIST+Schema.RESTRICTION+Schema.REPRODUCTION;
            } else {
                    int extend = 0;
                    int restrict = 0;
                    int reproduce = 0;

                    StringTokenizer t = new StringTokenizer (finalString, " ");
                    while (t.hasMoreTokens()) {
                            String token = t.nextToken ();

                            if ( token.equals (Schema.VAL_EQUIVCLASS) ) {
                                    if ( extend == 0 ) {
                                            extend = Schema.EQUIVCLASS;
                                    } else {
                                            reportGenericSchemaError ( "'equivClass' already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_EXTENSION) ) {
                                    if ( extend == 0 ) {
                                            extend = Schema.EXTENSION;
                                    } else {
                                            reportGenericSchemaError ( "extension already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_LIST) ) {
                                    if ( extend == 0 ) {
                                            extend = Schema.LIST;
                                    } else {
                                            reportGenericSchemaError ( "'list' already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_RESTRICTION) ) {
                                    if ( restrict == 0 ) {
                                            restrict = Schema.RESTRICTION;
                                    } else {
                                            reportGenericSchemaError ( "restriction already in set" );
                                    }
                            } else if ( token.equals (Schema.VAL_REPRODUCTION) ) {
                                    if ( reproduce == 0 ) {
                                            reproduce = Schema.REPRODUCTION;
                                    } else {
                                            reportGenericSchemaError ( "reproduction already in set" );
                                    }
                            } else {
                                    reportGenericSchemaError ( "Invalid final value (" + finalString + ")" );
                            }
                    }

                    return extend+restrict+reproduce;
            }
    }

    private void reportGenericSchemaError (String error) throws Exception {
            reportSchemaError (SchemaMessageProvider.GenericError, new Object[] { error });
    }

    private void reportSchemaError(int major, Object args[]) {
        try {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       SchemaMessageProvider.SCHEMA_DOMAIN,
                                       major,
                                       SchemaMessageProvider.MSG_NONE,
                                       args,
                                       XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class DatatypeValidatorRegistry {
        Hashtable fRegistry = new Hashtable();

        String integerSubtypeTable[][] = {
            { "non-negative-integer", DatatypeValidator.MININCLUSIVE , "0"},
            { "positive-integer", DatatypeValidator.MININCLUSIVE, "1"},
            { "non-positive-integer", DatatypeValidator.MAXINCLUSIVE, "0"},
            { "negative-integer", DatatypeValidator.MAXINCLUSIVE, "-1"}
        };

        void initializeRegistry() {
            Hashtable facets = null;
            //fRegistry.put("boolean", new BooleanValidator());
            //DatatypeValidator integerValidator = new IntegerValidator();
            //fRegistry.put("integer", integerValidator);
            //fRegistry.put("string", new StringValidator());
            //fRegistry.put("decimal", new DecimalValidator());
            //fRegistry.put("float", new FloatValidator());
            //fRegistry.put("double", new DoubleValidator());
            //fRegistry.put("timeDuration", new TimeDurationValidator());
            //fRegistry.put("timeInstant", new TimeInstantValidator());
            //fRegistry.put("binary", new BinaryValidator());
            //fRegistry.put("uri", new URIValidator());
            //REVISIT - enable the below
            //fRegistry.put("date", new DateValidator());
            //fRegistry.put("timePeriod", new TimePeriodValidator());
            //fRegistry.put("time", new TimeValidator());


            DatatypeValidator v = null;
            /*for (int i = 0; i < integerSubtypeTable.length; i++) {
                v = new IntegerValidator();
                facets = new Hashtable();
                facets.put(integerSubtypeTable[i][1],integerSubtypeTable[i][2]);
                v.setBasetype(integerValidator);
                try {
                    v.setFacets(facets);
                } catch (IllegalFacetException ife) {
                    System.out.println("Internal error initializing registry - Illegal facet: "+integerSubtypeTable[i][0]);
                } catch (IllegalFacetValueException ifve) {
                    System.out.println("Internal error initializing registry - Illegal facet value: "+integerSubtypeTable[i][0]);
                } catch (UnknownFacetException ufe) {
                    System.out.println("Internal error initializing registry - Unknown facet: "+integerSubtypeTable[i][0]);
                }
                fRegistry.put(integerSubtypeTable[i][0], v);
            }*/
        }

        DatatypeValidator getValidatorFor(String type) {
            return (DatatypeValidator) fRegistry.get(type);
        }

        void addValidator(String name, DatatypeValidator v) {
            fRegistry.put(name,v);
        }
    }


    //Unit Test here
    public static void main(String args[] ) {

        if( args.length != 1 ) {
            System.out.println( "Error: Usage java TraverseSchema yourFile.xsd" );
            System.exit(0);
        }

        DOMParser parser = new DOMParser() {
            public void ignorableWhitespace(char ch[], int start, int length) {}
            public void ignorableWhitespace(int dataIdx) {}
        };
        parser.setEntityResolver( new Resolver() );
        parser.setErrorHandler(  new ErrorHandler() );

        try {
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        }catch(  org.xml.sax.SAXNotRecognizedException e ) {
            e.printStackTrace();
        }catch( org.xml.sax.SAXNotSupportedException e ) {
            e.printStackTrace();
        }

        try {
        parser.parse( args[0]);
        }catch( IOException e ) {
            e.printStackTrace();
        }catch( SAXException e ) {
            e.printStackTrace();
        }

        Document     document   = parser.getDocument(); //Our Grammar

        OutputFormat    format  = new OutputFormat( document );
        java.io.StringWriter outWriter = new java.io.StringWriter();
        XMLSerializer    serial = new XMLSerializer( outWriter,format);

        TraverseSchema tst = null;
        try {
            Element root   = document.getDocumentElement();// This is what we pass to TraverserSchema
            //serial.serialize( root );
            //System.out.println(outWriter.toString());
            tst = new TraverseSchema( root );
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
            
            parser.getDocument();
    }

    static class Resolver implements EntityResolver {
        private static final String SYSTEM[] = {
            "http://www.w3.org/TR/2000/WD-xmlschema-1-20000407/structures.dtd",
            "http://www.w3.org/TR/2000/WD-xmlschema-1-20000407/datatypes.dtd",
            "http://www.w3.org/TR/2000/WD-xmlschema-1-20000407/versionInfo.ent",
        };
        private static final String PATH[] = {
            "structures.dtd",
            "datatypes.dtd",
            "versionInfo.ent",
        };

        public InputSource resolveEntity(String publicId, String systemId)
        throws IOException {

            // looking for the schema DTDs?
            for (int i = 0; i < SYSTEM.length; i++) {
                if (systemId.equals(SYSTEM[i])) {
                    InputSource source = new InputSource(getClass().getResourceAsStream(PATH[i]));
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            }

            // use default resolution
            return null;

        } // resolveEntity(String,String):InputSource

    } // class Resolver

    static class ErrorHandler implements org.xml.sax.ErrorHandler {

        /** Warning. */
        public void warning(SAXParseException ex) {
            System.err.println("[Warning] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        }

        /** Error. */
        public void error(SAXParseException ex) {
            System.err.println("[Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        }

        /** Fatal error. */
        public void fatalError(SAXParseException ex) throws SAXException {
            System.err.println("[Fatal Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
            throw ex;
        }

        //
        // Private methods
        //

        /** Returns a string of the location. */
        private String getLocationString(SAXParseException ex) {
            StringBuffer str = new StringBuffer();

            String systemId_ = ex.getSystemId();
            if (systemId_ != null) {
                int index = systemId_.lastIndexOf('/');
                if (index != -1)
                    systemId_ = systemId_.substring(index + 1);
                str.append(systemId_);
            }
            str.append(':');
            str.append(ex.getLineNumber());
            str.append(':');
            str.append(ex.getColumnNumber());

            return str.toString();

        } // getLocationString(SAXParseException):String
    }


}





