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

import org.apache.commons.lang3.tuple.Pair;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.CsvExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Implementation operations with external model.
 *
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
    private List<Pair<String, List<String>>> fileDescriptionsAndExtensions = new LinkedList<>();

    public ExternalModelServiceImpl() {
        Arrays.stream(ExternalModelType.values())
                .forEach(type -> {
                    type.extensions.forEach(extension -> extension2type.put(extension, type));
                    fileDescriptionsAndExtensions.add(Pair.of(type.description, type.extensions));
                });

        fileDescriptionsAndExtensions = Collections.unmodifiableList(fileDescriptionsAndExtensions);
    }

    public List<Pair<String, List<String>>> fileDescriptionsAndExtensions() {
        return fileDescriptionsAndExtensions;
    }

    public Pair<String, List<String>> fileDescriptionAndExtensions(String filterExtension) {
        return fileDescriptionsAndExtensions.stream()
                .filter(pair -> pair.getRight().contains(filterExtension))
                .findAny().orElse(null);
    }

    @Override
    public ExternalModel createExternalModelFromFile(File file, ModelNode parent) throws ExternalModelException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(parent);

        String fileExtension = Utils.getExtension(file.getName());
        ExternalModelType type = extension2type.get(fileExtension);
        if (type == null) {
            throw new IllegalArgumentException("Cannot defile external model for " + fileExtension + " type.");
        }
        ExternalModel externalModel;
        switch (type) {
            case EXCEL:
                externalModel = new ExcelExternalModel();
                break;
            case COMMA_SEPARATED_VALUES:
                externalModel = new CsvExternalModel();
                break;
            default:
                throw new AssertionError("Never must be thrown");
        }
        externalModel.setParent(parent);
        externalModel.initByFile(file);
        return externalModel;
    }

    @Override
    public void updateExternalModelFromFile(File file, ExternalModel externalModel) throws ExternalModelException {
        externalModel.initByFile(file);
        externalModel.updateCacheFromAttachment();
    }

    @Override
    public String makeExternalModelPath(ExternalModel externalModel) {
        String path = externalModel.getParent().getNodePath();
        path = path.replace(' ', '_');
        path = path.replace(ModelNode.NODE_SEPARATOR, File.separator);
        return path;
    }

    @Override
    public void storeExternalModel(ExternalModel externalModel, File folder) throws IOException {
        Objects.requireNonNull(externalModel);
        Objects.requireNonNull(folder);
        File file = new File(folder, externalModel.getName());
        Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
    }

}
