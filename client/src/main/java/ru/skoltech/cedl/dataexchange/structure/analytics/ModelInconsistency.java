package ru.skoltech.cedl.dataexchange.structure.analytics;

import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.external.ExternalModelCacheState;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by d.knoll on 6/26/2017.
 */
public class ModelInconsistency {

    private final String description;
    private final Severity severity;
    private final String sourceName;
    private final Object source;

    public ModelInconsistency(String description, Severity severity, String sourceName, Object source) {
        this.description = description;
        this.severity = severity;
        this.sourceName = sourceName;
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Object getSource() {
        return source;
    }

    public String getSourceName() {
        return sourceName;
    }

    public static List<ModelInconsistency> analyzeModel(Study study) {
        SystemModel systemModel = study.getSystemModel();
        Map<String, ParameterModel> parameterDictionary = systemModel.makeParameterDictionary();
        List<ModelInconsistency> modelInconsistencies = analyzeModel(systemModel, parameterDictionary);
        Long systemModelLatestModification = systemModel.findLatestModification();
        if (study.getLatestModelModification() < systemModelLatestModification) {
            modelInconsistencies.add(new ModelInconsistency("Study latest modification is earlier than system model last modification", Severity.ERROR, "study", study));
        }
        if (!study.getName().equals(study.getSystemModel().getName())) {
            modelInconsistencies.add(new ModelInconsistency("Study name and system model name differ", Severity.WARNING, "study", study));
        }
        return modelInconsistencies;
    }

    public static List<ModelInconsistency> analyzeModel(CompositeModelNode<? extends ModelNode> compositeModelNode, Map<String, ParameterModel> parameterDictionary) {
        LinkedList<ModelInconsistency> modelInconsistencies = new LinkedList<>();
        // external models
        for (ExternalModel em : compositeModelNode.getExternalModels()) {
            modelInconsistencies.addAll(analyzeModel(em));
        }
        // params
        for (ParameterModel parameterModel : compositeModelNode.getParameters()) {
            modelInconsistencies.addAll(analyzeModel(parameterModel, parameterDictionary));
        }
        // subnodes
        for (ModelNode modelNode : compositeModelNode.getSubNodes()) {
            if (modelNode instanceof CompositeModelNode) {
                modelInconsistencies.addAll(analyzeModel((CompositeModelNode<? extends ModelNode>) modelNode, parameterDictionary));
            }
        }
        return modelInconsistencies;
    }

    private static Collection<? extends ModelInconsistency> analyzeModel(ExternalModel em) {
        LinkedList<ModelInconsistency> modelInconsistencies = new LinkedList<>();
        if (em.getAttachment() == null) {
            modelInconsistencies.add(new ModelInconsistency("External model has empty attachment", Severity.CRITICAL, em.getNodePath(), em));
        }
        ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(em);
        if (cacheState == ExternalModelCacheState.CACHED_CONFLICTING_CHANGES) {
            modelInconsistencies.add(new ModelInconsistency("External model has be updated locally and remotely", Severity.CRITICAL, em.getNodePath(), em));
        } else if (cacheState == ExternalModelCacheState.CACHED_OUTDATED) {
            modelInconsistencies.add(new ModelInconsistency("External model is outdated", Severity.WARNING, em.getNodePath(), em));
        } else if (cacheState == ExternalModelCacheState.CACHED_MODIFIED_AFTER_CHECKOUT) {
            modelInconsistencies.add(new ModelInconsistency("External model has changes that need to be stored to the repository", Severity.WARNING, em.getNodePath(), em));
        }
        return modelInconsistencies;
    }

    private static Collection<ModelInconsistency> analyzeModel(ParameterModel pm, Map<String, ParameterModel> parameterDictionary) {
        LinkedList<ModelInconsistency> modelInconsistencies = new LinkedList<>();

        if (pm.getValue() == null) {
            modelInconsistencies.add(new ModelInconsistency("Parameter must have a value", Severity.CRITICAL, pm.getNodePath(), pm));
        }
        if (pm.getUnit() == null) {
            modelInconsistencies.add(new ModelInconsistency("Parameter should have a unit", Severity.WARNING, pm.getNodePath(), pm));
        }
        if (!pm.getIsReferenceValueOverridden() && pm.getOverrideValue() != null) {
            modelInconsistencies.add(new ModelInconsistency("Not overridden parameter must not have an override value", Severity.WARNING, pm.getNodePath(), pm));
        }

        if (pm.getValueSource() == ParameterValueSource.MANUAL) {
            if (pm.getIsReferenceValueOverridden()) {
                modelInconsistencies.add(new ModelInconsistency("Manual parameter can not be overridden", Severity.WARNING, pm.getNodePath(), pm));
            }
            if (pm.getOverrideValue() != null) {
                modelInconsistencies.add(new ModelInconsistency("Manual parameter must not have override value", Severity.WARNING, pm.getNodePath(), pm));
            }
            if (pm.getImportModel() != null || pm.getImportField() != null || pm.getValueReference() != null) {
                modelInconsistencies.add(new ModelInconsistency("Manual parameter must not have import reference", Severity.WARNING, pm.getNodePath(), pm));
            }
            if (pm.getCalculation() != null) {
                modelInconsistencies.add(new ModelInconsistency("Manual parameter must not have a calculation", Severity.WARNING, pm.getNodePath(), pm));
            }
        } else if (pm.getValueSource() == ParameterValueSource.LINK) {
            if (pm.getValueLink() == null) {
                modelInconsistencies.add(new ModelInconsistency("Linked parameter must have link to a source parameter", Severity.ERROR, pm.getNodePath(), pm));
            } else {
                // check link details
                ParameterModel actualSource = pm.getValueLink();
                String sourceUuid = actualSource.getUuid();
                ParameterModel sourceFromDictionary = parameterDictionary.get(sourceUuid);
                if (sourceFromDictionary == null) {
                    modelInconsistencies.add(new ModelInconsistency("Linked parameter points to source no longer part of the model", Severity.ERROR, pm.getNodePath(), pm));
                } else if (actualSource != sourceFromDictionary) {
                    modelInconsistencies.add(new ModelInconsistency("Linked parameter points to source outside of the model", Severity.ERROR, pm.getNodePath(), pm));
                }
            }
            if (pm.getIsReferenceValueOverridden() && pm.getOverrideValue() == null) {
                modelInconsistencies.add(new ModelInconsistency("Linked parameter with override, must have an override value", Severity.ERROR, pm.getNodePath(), pm));
            }
            if (pm.getImportModel() != null || pm.getImportField() != null || pm.getValueReference() != null) {
                modelInconsistencies.add(new ModelInconsistency("Linked parameter must not have import reference", Severity.WARNING, pm.getNodePath(), pm));
            }
            if (pm.getCalculation() != null) {
                modelInconsistencies.add(new ModelInconsistency("Linked parameter must not have a calculation", Severity.WARNING, pm.getNodePath(), pm));
            }
        } else if (pm.getValueSource() == ParameterValueSource.REFERENCE) {
            if (pm.getValueReference() == null) {
                modelInconsistencies.add(new ModelInconsistency("Reference parameter must a have import reference", Severity.ERROR, pm.getNodePath(), pm));
            } else {
                List<ExternalModel> externalModels = pm.getParent().getExternalModels();
                Map<String, ExternalModel> externalModelDictionary = externalModels.stream().collect(Collectors.toMap(ExternalModel::getUuid, Function.identity()));
                ExternalModel actualExternalModel = pm.getValueReference().getExternalModel();
                String uuid = actualExternalModel.getUuid();
                ExternalModel modelFromDictionary = externalModelDictionary.get(uuid);
                if (modelFromDictionary == null) {
                    modelInconsistencies.add(new ModelInconsistency("Referencing parameter points to an external model (" + actualExternalModel.getName() + ") no longer part of the model", Severity.ERROR, pm.getNodePath(), pm));
                } else if (actualExternalModel != modelFromDictionary) {
                    modelInconsistencies.add(new ModelInconsistency("Referencing parameter points to an external model outside of the model", Severity.ERROR, pm.getNodePath(), pm));
                }
            }
            if (pm.getIsReferenceValueOverridden() && pm.getOverrideValue() == null) {
                modelInconsistencies.add(new ModelInconsistency("Reference parameter with override, must have an override value", Severity.ERROR, pm.getNodePath(), pm));
            }
            if (pm.getCalculation() != null) {
                modelInconsistencies.add(new ModelInconsistency("Reference parameter must not have a calculation", Severity.WARNING, pm.getNodePath(), pm));
            }
        } else if (pm.getValueSource() == ParameterValueSource.CALCULATION) {
            if (pm.getCalculation() == null) {
                modelInconsistencies.add(new ModelInconsistency("Calculated parameter must have a calculation", Severity.ERROR, pm.getNodePath(), pm));
            } else {
                Calculation calculation = pm.getCalculation();
                for (Argument argument : calculation.getArguments()) {
                    if (argument instanceof Argument.Parameter) {
                        ParameterModel actualSource = ((Argument.Parameter) argument).getLink();
                        String sourceUuid = actualSource.getUuid();
                        ParameterModel sourceFromDictionary = parameterDictionary.get(sourceUuid);
                        if (sourceFromDictionary == null) {
                            modelInconsistencies.add(new ModelInconsistency("Calculation points to source no longer part of the model", Severity.ERROR, pm.getNodePath(), pm));
                        } else if (actualSource != sourceFromDictionary) {
                            modelInconsistencies.add(new ModelInconsistency("Calculation points to source outside of the model", Severity.ERROR, pm.getNodePath(), pm));
                        }
                    }
                }
            }
            if (pm.getIsReferenceValueOverridden() && pm.getOverrideValue() == null) {
                modelInconsistencies.add(new ModelInconsistency("Calculated parameter with override, must have an override value", Severity.ERROR, pm.getNodePath(), pm));
            }
            if (pm.getImportModel() != null || pm.getImportField() != null || pm.getValueReference() != null) {
                modelInconsistencies.add(new ModelInconsistency("Calculated parameter must not have import reference", Severity.WARNING, pm.getNodePath(), pm));
            }
        }

        if (pm.getNature() == ParameterNature.INPUT || pm.getNature() == ParameterNature.INTERNAL) {
            List<ParameterModel> dependentParameters = ProjectContext.getInstance().getProject().getParameterLinkRegistry().getDependentParameters(pm);
            if (dependentParameters.size() > 0) {
                String parameterNames = dependentParameters.stream().map(ParameterModel::getNodePath).collect(Collectors.joining(", "));
                modelInconsistencies.add(new ModelInconsistency("Input parameter must not be used as a source by other parameters: " + parameterNames, Severity.ERROR, pm.getNodePath(), pm));
            }
        }
        if (pm.getNature() == ParameterNature.INTERNAL || pm.getNature() == ParameterNature.OUTPUT) {
            if (pm.getValueSource() == ParameterValueSource.LINK) {
                modelInconsistencies.add(new ModelInconsistency("Internal or Output parameter must not be linked", Severity.WARNING, pm.getNodePath(), pm));
            }
        }
        if (pm.getNature() == ParameterNature.OUTPUT) {
            List<ParameterModel> dependentParameters = ProjectContext.getInstance().getProject().getParameterLinkRegistry().getDependentParameters(pm);
            if (dependentParameters.size() == 0) {
                modelInconsistencies.add(new ModelInconsistency("Output parameter is not linked by any other parameter", Severity.WARNING, pm.getNodePath(), pm));
            }
        }

        return modelInconsistencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelInconsistency that = (ModelInconsistency) o;

        if (!description.equals(that.description)) return false;
        if (severity != that.severity) return false;
        return sourceName.equals(that.sourceName);
    }

    @Override
    public int hashCode() {
        int result = description.hashCode();
        result = 31 * result + severity.hashCode();
        result = 31 * result + sourceName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ModelInconsistency{" +
                "description='" + description + '\'' +
                ", severity=" + severity +
                ", sourceName='" + sourceName + '\'' +
                '}';
    }

    public enum Severity {
        WARNING, ERROR, CRITICAL
    }
}
