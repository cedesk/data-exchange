/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.service.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.CsvExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ExternalModelService}.
 * <p/>
 * Created by Nikolay Groshkov on 31-Aug-17.
 */
public class ExternalModelServiceImpl implements ExternalModelService {

    private enum ExternalModelType {
        EXCEL("Excel Spreadsheets", XLS, XLSX, XLSM),
        COMMA_SEPARATED_VALUES("Comma Separated Values Files", CSV);

        final String description;
        final List<String> extensions;

        ExternalModelType(String description, String... extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }
    }

    private Map<String, ExternalModelType> extension2type = new HashMap<>();
    private List<String> supportedExtensions = new LinkedList<>();

    public ExternalModelServiceImpl() {
        Arrays.stream(ExternalModelType.values())
                .forEach(type -> {
                    type.extensions.forEach(extension -> extension2type.put(extension, type));
                    List<String> adapted = type.extensions.stream().map(extension -> "*" + extension).collect(Collectors.toList());
                    supportedExtensions.addAll(adapted);
                });

        supportedExtensions = Collections.unmodifiableList(supportedExtensions);
    }

    public List<String> supportedExtensions() {
        return supportedExtensions;
    }

    public Pair<String, List<String>> fileDescriptionAndExtensions(String filterExtension) {
        return Arrays.stream(ExternalModelType.values())
                .filter(externalModelType -> externalModelType.extensions.contains(filterExtension))
                .map(externalModelType -> {
                    List<String> extensions = externalModelType.extensions.stream()
                            .map(extension -> "*" + extension).collect(Collectors.toList());
                    return Pair.of(externalModelType.description, extensions);
                })
                .findFirst().orElse(null);
    }

    @Override
    public ExternalModel cloneExternalModel(ExternalModel externalModel) {
        Objects.requireNonNull(externalModel);

        return this.cloneExternalModel(externalModel, externalModel.getParent());
    }

    @Override
    public ExternalModel cloneExternalModel(ExternalModel externalModel, ModelNode parent) {
        Objects.requireNonNull(externalModel);
        Objects.requireNonNull(parent);

        ExternalModel newExternalModel = createExternalModel(externalModel.getName());
        newExternalModel.setName(externalModel.getName());
        newExternalModel.setAttachment(externalModel.getAttachment());
        newExternalModel.setLastModification(externalModel.getLastModification());
        newExternalModel.setParent(parent);
        newExternalModel.init();
        return newExternalModel;
    }

    @Override
    public ExternalModel createExternalModelFromFile(File file, ModelNode parent) throws ExternalModelException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(parent);

        ExternalModel externalModel = createExternalModel(file.getName());
        externalModel.setParent(parent);
        externalModel.initByFile(file);
        return externalModel;
    }

    private ExternalModel createExternalModel(String fileName) {
        String fileExtension = FilenameUtils.getExtension(fileName);
        ExternalModelType type = extension2type.get("." + fileExtension);
        if (type == null) {
            throw new IllegalArgumentException("Cannot defile external model for " + fileExtension + " type.");
        }

        switch (type) {
            case EXCEL:
                return new ExcelExternalModel();
            case COMMA_SEPARATED_VALUES:
                return new CsvExternalModel();
            default:
                throw new AssertionError("Never must be thrown");
        }
    }

    @Override
    public void updateExternalModelFromFile(File file, ExternalModel externalModel) throws ExternalModelException {
        externalModel.initByFile(file);
        externalModel.updateCacheFromAttachment();
    }

    @Override
    public String makeExternalModelPath(ExternalModel externalModel) {
        Objects.requireNonNull(externalModel);
        Objects.requireNonNull(externalModel.getParent());
        Objects.requireNonNull(externalModel.getParent().getNodePath());

        String path = externalModel.getParent().getNodePath();
        path = path.replace(' ', '_');
        path = path.replace(ModelNode.NODE_SEPARATOR, File.separator);
        return path;
    }

    @Override
    public String makeExternalModelZipPath(ExternalModel externalModel) {
        Objects.requireNonNull(externalModel);
        Objects.requireNonNull(externalModel.getParent());
        Objects.requireNonNull(externalModel.getParent().getNodePath());

        String externalModelPath = this.makeExternalModelPath(externalModel);
        if (externalModelPath.startsWith("/") || externalModelPath.startsWith("\\")) {
            externalModelPath = externalModelPath.substring(1);
        }
        return externalModelPath + "/";
    }

}
