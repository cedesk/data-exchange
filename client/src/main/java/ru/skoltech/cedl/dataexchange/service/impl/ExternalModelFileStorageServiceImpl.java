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

import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.service.ExternalModelFileStorageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Created by Nikolay Groshkov on 31-Aug-17.
 */
public class ExternalModelFileStorageServiceImpl implements ExternalModelFileStorageService {

    @Override
    public ExternalModel createExternalModelFromFile(File file, ModelNode parent) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        String fileName = file.getName();
        ExternalModel externalModel = new ExcelExternalModel();
        externalModel.setName(fileName);
        externalModel.setAttachment(Files.readAllBytes(path));
        externalModel.setLastModification(file.lastModified());
        externalModel.setParent(parent);
        return externalModel;
    }

    @Override
    public ExternalModel readExternalModelAttachmentFromFile(File file, ExternalModel externalModel) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        externalModel.setAttachment(Files.readAllBytes(path));
        externalModel.setLastModification(file.lastModified());
        return externalModel;
    }

    @Override
    public void storeExternalModel(ExternalModel externalModel, File folder) throws IOException {
        Objects.requireNonNull(externalModel);
        Objects.requireNonNull(folder);
        File file = new File(folder, externalModel.getName());
        Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
    }

    @Override
    public String makeExternalModelPath(ExternalModel externalModel) {
        String path = externalModel.getParent().getNodePath();
        path = path.replace(' ', '_');
        path = path.replace(ModelNode.NODE_SEPARATOR, File.separator);
        return path;
    }

    @Override
    public File createFilePathForExternalModel(File projectDataDir, ExternalModel externalModel) {
        String nodePath = this.makeExternalModelPath(externalModel);
        File nodeDir = new File(projectDataDir, nodePath);
        String rectifiedFileName = externalModel.getId() + "_" + externalModel.getName().replace(' ', '_');
        return new File(nodeDir, rectifiedFileName);
    }


}
