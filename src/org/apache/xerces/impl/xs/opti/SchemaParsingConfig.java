/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.xs.opti;

import java.io.IOException;
import java.util.Locale;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLDTDScannerImpl;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.XMLNSDocumentScannerImpl;
import org.apache.xerces.impl.dv.DTDDVFactory;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.parsers.BasicParserConfiguration;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
import org.w3c.dom.Document;


/**
 * @author Rahul Srivastava, Sun Microsystems Inc.
 *
 * @version $Id$
 */
public class SchemaParsingConfig extends BasicParserConfiguration 
                                 implements XMLPullParserConfiguration {

    //
    // Constants
    //

    // feature identifiers

    /** Feature identifier: warn on duplicate attribute definition. */
    protected static final String WARN_ON_DUPLICATE_ATTDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_DUPLICATE_ATTDEF_FEATURE;

    /** Feature identifier: warn on duplicate entity definition. */
    //  protected static final String WARN_ON_DUPLICATE_ENTITYDEF = Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_DUPLICATE_ENTITYDEF_FEATURE;
    
    /** Feature identifier: warn on undeclared element definition. */
    protected static final String WARN_ON_UNDECLARED_ELEMDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_UNDECLARED_ELEMDEF_FEATURE;
    
    /** Feature identifier: allow Java encodings. */
    protected static final String ALLOW_JAVA_ENCODINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    
    /** Feature identifier: continue after fatal error. */
    protected static final String CONTINUE_AFTER_FATAL_ERROR = 
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;

    /** Feature identifier: load external DTD. */
    protected static final String LOAD_EXTERNAL_DTD =
        Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE;

    /** Feature identifier: notify built-in refereces. */
    protected static final String NOTIFY_BUILTIN_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_BUILTIN_REFS_FEATURE;
    
    /** Feature identifier: notify character refereces. */
    protected static final String NOTIFY_CHAR_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_CHAR_REFS_FEATURE;
    

    /** Feature identifier: expose schema normalized value */
    protected static final String NORMALIZE_DATA =
    Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;


    /** Feature identifier: send element default value via characters() */
    protected static final String SCHEMA_ELEMENT_DEFAULT =
    Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_ELEMENT_DEFAULT;
    	

    // property identifiers

    /** Property identifier: error reporter. */
    protected static final String ERROR_REPORTER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    /** Property identifier: entity manager. */
    protected static final String ENTITY_MANAGER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    
    /** Property identifier document scanner: */
    protected static final String DOCUMENT_SCANNER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DOCUMENT_SCANNER_PROPERTY;

    /** Property identifier: DTD scanner. */
    protected static final String DTD_SCANNER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;

    /** Property identifier: grammar pool. */
    protected static final String XMLGRAMMAR_POOL = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    
    /** Property identifier: DTD validator. */
    protected static final String DTD_VALIDATOR = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_VALIDATOR_PROPERTY;

    /** Property identifier: namespace binder. */
    protected static final String NAMESPACE_BINDER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_BINDER_PROPERTY;

    /** Property identifier: datatype validator factory. */
    protected static final String DATATYPE_VALIDATOR_FACTORY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DATATYPE_VALIDATOR_FACTORY_PROPERTY;

    protected static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;

    /** Property identifier: XML Schema validator. */
    protected static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;


    // debugging

    /** Set to true and recompile to print exception stack trace. */
    private static final boolean PRINT_EXCEPTION_STACK_TRACE = false;

    //
    // Data
    //

    // components (non-configurable)

    /** Grammar pool. */
    protected XMLGrammarPool fGrammarPool;

    /** Datatype validator factory. */
    protected DTDDVFactory fDatatypeValidatorFactory;

    // components (configurable)

    /** Error reporter. */
    protected XMLErrorReporter fErrorReporter;

    /** Entity manager. */
    protected XMLEntityManager fEntityManager;

    /** Document scanner. */
    protected XMLDocumentScanner fScanner;

    /** Input Source */
    protected XMLInputSource fInputSource;

    /** DTD scanner. */
    protected XMLDTDScanner fDTDScanner;

    
    protected SchemaDOMParser fSchemaDOMParser;

    protected ValidationManager fValidationManager;
    // state

    /** Locator */
    protected XMLLocator fLocator;

    /** 
     * True if a parse is in progress. This state is needed because
     * some features/properties cannot be set while parsing (e.g.
     * validation and namespaces).
     */
    protected boolean fParseInProgress = false;

    //
    // Constructors
    //

    /** Default constructor. */
    public SchemaParsingConfig() {
        this(null, null, null);
    } // <init>()

    /** 
     * Constructs a parser configuration using the specified symbol table. 
     *
     * @param symbolTable The symbol table to use.
     */
    public SchemaParsingConfig(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } // <init>(SymbolTable)

    /**
     * Constructs a parser configuration using the specified symbol table and
     * grammar pool.
     * <p>
     * <strong>REVISIT:</strong> 
     * Grammar pool will be updated when the new validation engine is
     * implemented.
     *
     * @param symbolTable The symbol table to use.
     * @param grammarPool The grammar pool to use.
     */
    public SchemaParsingConfig(SymbolTable symbolTable,
                                       XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } // <init>(SymbolTable,XMLGrammarPool)

    /**
     * Constructs a parser configuration using the specified symbol table,
     * grammar pool, and parent settings.
     * <p>
     * <strong>REVISIT:</strong> 
     * Grammar pool will be updated when the new validation engine is
     * implemented.
     *
     * @param symbolTable    The symbol table to use.
     * @param grammarPool    The grammar pool to use.
     * @param parentSettings The parent settings.
     */
    public SchemaParsingConfig(SymbolTable symbolTable,
                                       XMLGrammarPool grammarPool,
                                       XMLComponentManager parentSettings) {
        super(symbolTable, parentSettings);

        // add default recognized features
        final String[] recognizedFeatures = {
			PARSER_SETTINGS, WARN_ON_DUPLICATE_ATTDEF,   WARN_ON_UNDECLARED_ELEMDEF,
            ALLOW_JAVA_ENCODINGS,       CONTINUE_AFTER_FATAL_ERROR,
            LOAD_EXTERNAL_DTD,          NOTIFY_BUILTIN_REFS,
            NOTIFY_CHAR_REFS
        };
        addRecognizedFeatures(recognizedFeatures);
		fFeatures.put(PARSER_SETTINGS, Boolean.TRUE);
        // set state for default features
		fFeatures.put(WARN_ON_DUPLICATE_ATTDEF, Boolean.FALSE);
        //setFeature(WARN_ON_DUPLICATE_ENTITYDEF, false);
		fFeatures.put(WARN_ON_UNDECLARED_ELEMDEF, Boolean.FALSE);
		fFeatures.put(ALLOW_JAVA_ENCODINGS, Boolean.FALSE);
		fFeatures.put(CONTINUE_AFTER_FATAL_ERROR, Boolean.FALSE);
		fFeatures.put(LOAD_EXTERNAL_DTD, Boolean.TRUE);
		fFeatures.put(NOTIFY_BUILTIN_REFS, Boolean.FALSE);
		fFeatures.put(NOTIFY_CHAR_REFS, Boolean.FALSE);

        // add default recognized properties
        final String[] recognizedProperties = {
            ERROR_REPORTER,             
            ENTITY_MANAGER, 
            DOCUMENT_SCANNER,
            DTD_SCANNER,
            DTD_VALIDATOR,
            NAMESPACE_BINDER,
            XMLGRAMMAR_POOL,   
            DATATYPE_VALIDATOR_FACTORY,
            VALIDATION_MANAGER
        };
        addRecognizedProperties(recognizedProperties);
	
        fGrammarPool = grammarPool;
        if(fGrammarPool != null){
        	setProperty(XMLGRAMMAR_POOL, fGrammarPool);
        }

        fEntityManager = new XMLEntityManager();
        fProperties.put(ENTITY_MANAGER, fEntityManager);
        addComponent(fEntityManager);

        fErrorReporter = new XMLErrorReporter();
        fErrorReporter.setDocumentLocator(fEntityManager.getEntityScanner());
		fProperties.put(ERROR_REPORTER, fErrorReporter);
        addComponent(fErrorReporter);

        fScanner = new XMLNSDocumentScannerImpl();
		fProperties.put(DOCUMENT_SCANNER, fScanner);
		addComponent((XMLComponent)fScanner);

        fDTDScanner = new XMLDTDScannerImpl();
		fProperties.put(DTD_SCANNER, fDTDScanner);
        addComponent((XMLComponent)fDTDScanner);

        
        fDatatypeValidatorFactory = DTDDVFactory.getInstance();;
		fProperties.put(DATATYPE_VALIDATOR_FACTORY,
                        fDatatypeValidatorFactory);
        
        fValidationManager = new ValidationManager();
		fProperties.put(VALIDATION_MANAGER, fValidationManager);

        // add message formatters
        if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }
        
		if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
			XSMessageFormatter xmft = new XSMessageFormatter();
			fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
		}

        // set locale
        try {
            setLocale(Locale.getDefault()); 
        }
        catch (XNIException e) {
            // do nothing
            // REVISIT: What is the right thing to do? -Ac
        }

    } // <init>(SymbolTable,XMLGrammarPool)

    //
    // Public methods
    //

    /**
     * Set the locale to use for messages.
     *
     * @param locale The locale object to use for localization of messages.
     *
     * @exception XNIException Thrown if the parser does not support the
     *                         specified locale.
     */
    public void setLocale(Locale locale) throws XNIException {
        super.setLocale(locale);
        fErrorReporter.setLocale(locale);
    } // setLocale(Locale)

    //
    // XMLPullParserConfiguration methods
    //

    // parsing

    /**
     * Sets the input source for the document to parse.
     *
     * @param inputSource The document's input source.
     *
     * @exception XMLConfigurationException Thrown if there is a 
     *                        configuration error when initializing the
     *                        parser.
     * @exception IOException Thrown on I/O error.
     *
     * @see #parse(boolean)
     */
    public void setInputSource(XMLInputSource inputSource)
        throws XMLConfigurationException, IOException {
        
        // REVISIT: this method used to reset all the components and
        //          construct the pipeline. Now reset() is called
        //          in parse (boolean) just before we parse the document
        //          Should this method still throw exceptions..?

        fInputSource = inputSource;
        
    } // setInputSource(XMLInputSource)

    /**
     * Parses the document in a pull parsing fashion.
     *
     * @param complete True if the pull parser should parse the
     *                 remaining document completely.
     *
     * @returns True if there is more document to parse.
     *
     * @exception XNIException Any XNI exception, possibly wrapping 
     *                         another exception.
     * @exception IOException  An IO exception from the parser, possibly
     *                         from a byte stream or character stream
     *                         supplied by the parser.
     *
     * @see #setInputSource
     */
    public boolean parse(boolean complete) throws XNIException, IOException {
        //
        // reset and configure pipeline and set InputSource.
        if (fInputSource !=null) {
            try {
                // resets and sets the pipeline.
                reset();
                fScanner.setInputSource(fInputSource);
                fInputSource = null;
            } 
            catch (XNIException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } 
            catch (IOException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } 
            catch (RuntimeException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            }
            catch (Exception ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw new XNIException(ex);
            }
        }

        try {
            return fScanner.scanDocument(complete);
        } 
        catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (RuntimeException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }
        catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }

    } // parse(boolean):boolean

    /**
     * If the application decides to terminate parsing before the xml document
     * is fully parsed, the application should call this method to free any
     * resource allocated during parsing. For example, close all opened streams.
     */
    public void cleanup() {
        fEntityManager.closeReaders();
    }
    
    //
    // XMLParserConfiguration methods
    //

    /**
     * Parses the specified input source.
     *
     * @param source The input source.
     *
     * @exception XNIException Throws exception on XNI error.
     * @exception java.io.IOException Throws exception on i/o error.
     */
    public void parse(XMLInputSource source) throws XNIException, IOException {

        if (fParseInProgress) {
            // REVISIT - need to add new error message
            throw new XNIException("FWK005 parse may not be called while parsing.");
        }
        fParseInProgress = true;

        try {
            setInputSource(source);
            parse(true);
        } 
        catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }
        catch (RuntimeException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }              
        catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }
        finally {
            fParseInProgress = false;
            // close all streams opened by xerces
            this.cleanup();
        }

    } // parse(InputSource)

    //
    // Protected methods
    //
    
    /** 
     * Reset all components before parsing. 
     *
     * @throws XNIException Thrown if an error occurs during initialization.
     */
    public void reset() throws XNIException {

        // set handlers
        if (fSchemaDOMParser == null)
        	fSchemaDOMParser = new SchemaDOMParser(this);
        fDocumentHandler = fSchemaDOMParser;
        fDTDHandler = fSchemaDOMParser;
        fDTDContentModelHandler = fSchemaDOMParser;

        // configure the pipeline and initialize the components
        configurePipeline();
        super.reset();

    } // reset()

    /** Configures the pipeline. */
    protected void configurePipeline() {

        // setup document pipeline
        fScanner.setDocumentHandler(fDocumentHandler);
        fDocumentHandler.setDocumentSource(fScanner);
        fLastComponent = fScanner;

        // setup dtd pipeline
        if (fDTDScanner != null) {
                fDTDScanner.setDTDHandler(fDTDHandler);
                fDTDScanner.setDTDContentModelHandler(fDTDContentModelHandler);
        }


    } // configurePipeline()

    // features and properties

    /**
     * Check a feature. If feature is know and supported, this method simply
     * returns. Otherwise, the appropriate exception is thrown.
     *
     * @param featureId The unique identifier (URI) of the feature.
     *
     * @throws XMLConfigurationException Thrown for configuration error.
     *                                   In general, components should
     *                                   only throw this exception if
     *                                   it is <strong>really</strong>
     *                                   a critical error.
     */
    protected void checkFeature(String featureId)
        throws XMLConfigurationException {

        //
        // Xerces Features
        //

        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            String feature = featureId.substring(Constants.XERCES_FEATURE_PREFIX.length());
            //
            // http://apache.org/xml/features/validation/dynamic
            //   Allows the parser to validate a document only when it
            //   contains a grammar. Validation is turned on/off based
            //   on each document instance, automatically.
            //
            if (feature.equals(Constants.DYNAMIC_VALIDATION_FEATURE)) {
                return;
            }
            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if (feature.equals(Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE)) {
                // REVISIT
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if (feature.equals(Constants.VALIDATE_CONTENT_MODELS_FEATURE)) {
                // REVISIT
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            //
            // http://apache.org/xml/features/validation/nonvalidating/load-dtd-grammar
            //
            if (feature.equals(Constants.LOAD_DTD_GRAMMAR_FEATURE)) {
                return;
            }
            //
            // http://apache.org/xml/features/validation/nonvalidating/load-external-dtd
            //
            if (feature.equals(Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                return;
            }

            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if (feature.equals(Constants.VALIDATE_DATATYPES_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
        }

        //
        // Not recognized
        //

        super.checkFeature(featureId);

    } // checkFeature(String)

    /**
     * Check a property. If the property is know and supported, this method
     * simply returns. Otherwise, the appropriate exception is thrown.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     *
     * @throws XMLConfigurationException Thrown for configuration error.
     *                                   In general, components should
     *                                   only throw this exception if
     *                                   it is <strong>really</strong>
     *                                   a critical error.
     */
    protected void checkProperty(String propertyId)
        throws XMLConfigurationException {

        //
        // Xerces Properties
        //

        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            String property = propertyId.substring(Constants.XERCES_PROPERTY_PREFIX.length());
            if (property.equals(Constants.DTD_SCANNER_PROPERTY)) {
                return;
            }
        }

        if (propertyId.startsWith(Constants.JAXP_PROPERTY_PREFIX)) {
            String property =
                propertyId.substring(Constants.JAXP_PROPERTY_PREFIX.length());
            if (property.equals(Constants.SCHEMA_SOURCE)) {
                return;
            }
        }

        //
        // Not recognized
        //

        super.checkProperty(propertyId);

    } // checkProperty(String)

    
    
    //
    // other methods
    //
    
    /** Returns the Document object. */
    public Document getDocument() {
        return fSchemaDOMParser.getDocument();
    }

    /** */
    public void resetNodePool() {
        // REVISIT: to implement: introduce a node pool to reuse DTM nodes.
        //          reset this pool here.
    }
    
}
