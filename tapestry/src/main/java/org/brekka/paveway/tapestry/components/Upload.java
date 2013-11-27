/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.paveway.tapestry.components;

import java.lang.reflect.Field;
import java.text.Format;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.Binding;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.FieldValidationSupport;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationDecorator;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.brekka.commons.lang.ByteLengthFormat;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.model.UploadedFileInfo;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.web.model.UploadingFilesContext;
import org.brekka.paveway.web.session.UploadsContext;
import org.brekka.paveway.web.upload.EncryptedFileItem;

/**
 * A component to upload files that enhances the default tapestry component with advanced HTML5 multi-file-upload support.
 * It will still fall back to plain file upload if HTML5 features are unavailable.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 * @author Original authors of org.apache.tapestry5.upload.components.Upload that this is based on
 * @see org.apache.tapestry5.upload.components.Upload
 */
@Events(EventConstants.VALIDATE)
@Import(library = {
    "classpath:org/brekka/paveway/tapestry/vendor/jquery.ui.widget.js",
    "classpath:org/brekka/paveway/tapestry/jquery.iframe-transport.js",
    "classpath:org/brekka/paveway/tapestry/jquery.fileupload.js",
    "classpath:org/brekka/paveway/tapestry/upload.js"
}, stylesheet= "classpath:org/brekka/paveway/tapestry/upload.css")
public class Upload extends AbstractField {
    public static final String MULTIPART_ENCTYPE = "multipart/form-data";

    /**
     * The uploaded file value
     */
    @Parameter(required = false, principal = true, autoconnect = true)
    private UploadedFiles value;

    /**
     * The object that will perform input validation. The "validate:" binding prefix is generally used to provide this
     * object in a declarative fashion.
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE)
    private FieldValidator<Object> validate;


    @Parameter(required = false, defaultPrefix = BindingConstants.PROP)
    private UploadPolicy uploadPolicy;

    @Parameter(required = false, defaultPrefix = BindingConstants.LITERAL)
    private String callback;

    @Property
    protected List<UploadedFileInfo> uploadedFiles;

    @Property
    protected UploadedFileInfo loopFile;

    @Environmental
    private ValidationTracker tracker;

    @Inject
    private MultipartDecoder decoder;

    @Inject
    protected RequestGlobals requestGlobals;

    @Environmental
    private FormSupport formSupport;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private ComponentResources resources;

    @Inject
    private Locale locale;

    @Inject
    private FieldValidationSupport fieldValidationSupport;

    @Mixin
    private RenderDisabled renderDisabled;

    @Inject
    @Path("classpath:org/apache/tapestry5/upload/components/upload.js")
    private Asset uploadScript;

    @Inject
    private Request request;

    @Property
    protected Format byteLengthFormat = new ByteLengthFormat(this.resources.getLocale(), ByteLengthFormat.Mode.SI);

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @Inject
    private Messages messages;


    private String keyControlName;

    private String makeKey;

    private String outerId;

    private UploadedFiles files;


    public void init(final String makeKey) {
        this.makeKey = makeKey;
    }

    /**
     * Computes a default value for the "validate" parameter using {@link FieldValidatorDefaultSource}.
     */
    final Binding defaultValidate() {
        return this.defaultProvider.defaultValidatorBinding("value", this.resources);
    }


    @SetupRender
    void setupRender() {
        String keyControlName = this.formSupport.allocateControlName(getClientId() + "Key");
        this.formSupport.storeAndExecute(this, new Setup(keyControlName));
        this.formSupport.store(this, new UploadProcessSubmission());
        this.outerId = this.javaScriptSupport.allocateClientId(getClientId() + "_outer");
    }

    /**
     *
     */
    public void processMultiUpload() {
        UploadedFile uploaded = this.decoder.getFileUpload(getControlName());

        if (uploaded != null) {
            if (uploaded.getFileName() == null || uploaded.getFileName().length() == 0) {
                uploaded = null;
            }
        }

        try {
            this.fieldValidationSupport.validate(uploaded, this.resources, this.validate);
        } catch (ValidationException ex) {
            this.tracker.recordError(this, ex.getMessage());
        }

        String key = this.request.getParameter(this.keyControlName);
        this.files = resolveFiles(key);

        if (uploaded != null) {
            this.files.discard();
            Object object;
            try {
                Field field = uploaded.getClass().getDeclaredField("item");
                field.setAccessible(true);
                object = field.get(uploaded);
                if (object instanceof EncryptedFileItem) {
                    EncryptedFileItem encryptedFileItem = (EncryptedFileItem) object;
                    FileBuilder fileBuilder = encryptedFileItem.complete(null);
                    ((UploadingFilesContext) this.files).retain(fileBuilder.getFileName(), fileBuilder);
                } else {
                    throw new IllegalStateException();
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        this.value = this.files;
    }

    private <T extends UploadedFiles & UploadingFilesContext> T resolveFiles(final String key) {
        HttpServletRequest req = this.requestGlobals.getHTTPServletRequest();
        UploadsContext uploadContext = UploadsContext.get(req, true);
        T files = null;
        if (uploadContext != null) {
            files = uploadContext.get(key, this.uploadPolicy);
        }

        if (files == null) {
            throw new IllegalStateException("Allocation maker is null");
        }
        return files;
    }

    /**
     * Render the upload tags.
     *
     * @param writer
     *            Writer to output markup
     */
    protected boolean beginRender(final MarkupWriter writer) {
        this.formSupport.setEncodingType(MULTIPART_ENCTYPE);

        writer.element("div", "id", this.outerId);

        // Standard input element as would be found in the regular file upload.
        writer.element("div", "class" , "pw-failsafe-input");
        writer.element("input", "type", "file", "name", getControlName(), "id", getClientId());
        writer.end();

        this.validate.render(writer);

        this.resources.renderInformalParameters(writer);

        decorateInsideField();

        // TAPESTRY-2453
        if (this.request.isXHR()) {
            this.javaScriptSupport.importJavaScriptLibrary(this.uploadScript);
            this.javaScriptSupport.addInitializerCall("injectedUpload", getClientId());
        }
        if (this.makeKey == null) {
            this.makeKey = RandomStringUtils.randomAlphabetic(4);
        }
        String compoundMakeKey = this.makeKey + "_" + getControlName();

        writer.end();
        writer.element("input", "type", "hidden", "name", this.keyControlName, "value", compoundMakeKey);
        writer.end();

        UploadedFiles files = resolveFiles(compoundMakeKey);
        UploadPolicy policy = files.getPolicy();
        HttpServletRequest req = this.requestGlobals.getHTTPServletRequest();
        String uploadLink = req.getContextPath() + "/upload/" + compoundMakeKey;
        String callback = this.callback;
        if (callback == null) {
            callback = "function(ready) {}";
        }
        this.javaScriptSupport.addScript("PavewayUpload.apply('%s', '%s', %d, %d, %d, %s);",
                this.outerId, uploadLink, policy.getMaxFiles(), policy.getMaxFileSize(), policy.getClusterSize(), callback);

        this.uploadedFiles = files.previewReady();
        return true;
    }

    public void afterRender(final MarkupWriter writer) {
        writer.end();
    }

    /**
     * @return the value
     */
    public UploadedFiles getValue() {
        return this.value;
    }

    /**
     * Setup the key control
     * @param keyControlName
     */
    protected void setupKeyControlName(final String keyControlName) {
        this.keyControlName = keyControlName;
    }

    @Override
    protected void processSubmission(final String controlName) {
        // Ignore, handle using processMultiUpload instead
    }

    Upload injectDecorator(final ValidationDecorator decorator) {
        setDecorator(decorator);
        return this;
    }

    Upload injectRequest(final Request request) {
        this.request = request;
        return this;
    }

    Upload injectFormSupport(final FormSupport formSupport) {
        // We have our copy ...
        this.formSupport = formSupport;

        // As does AbstractField
        setFormSupport(formSupport);

        return this;
    }

    Upload injectFieldValidator(final FieldValidator<Object> validator) {
        this.validate = validator;

        return this;
    }

    static class Setup implements ComponentAction<Upload> {

        /**
         * Serial UID
         */
        private static final long serialVersionUID = -5408637002364015834L;
        private final String controlName;

        public Setup(final String controlName) {
            this.controlName = controlName;
        }

        @Override
        public void execute(final Upload component) {
            component.setupKeyControlName(this.controlName);
        }

        @Override
        public String toString() {
            return String.format("Upload.Setup[%s]", this.controlName);
        }
    }

    static class UploadProcessSubmission implements ComponentAction<Upload> {
        private static final long serialVersionUID = -4346426414137434418L;

        @Override
        public void execute(final Upload component) {
            component.processMultiUpload();
        }

        @Override
        public String toString() {
            return "Upload.UploadProcessSubmission";
        }
    }

}
