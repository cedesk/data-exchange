package ru.skoltech.cedl.dataexchange.tradespace;

/**
 * Created by d.knoll on 6/28/2017.
 */
public class ModelStateLink {

    private int studyRevisionId;

    public ModelStateLink(int studyRevisionId) {
        this.studyRevisionId = studyRevisionId;
    }

    public int getStudyRevisionId() {
        return studyRevisionId;
    }

    public void setStudyRevisionId(int studyRevisionId) {
        this.studyRevisionId = studyRevisionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelStateLink modelStateLink = (ModelStateLink) o;

        return studyRevisionId == modelStateLink.studyRevisionId;
    }

    @Override
    public int hashCode() {
        return studyRevisionId;
    }

    @Override
    public String toString() {
        return "ModelStateLink{" +
                "studyRevisionId=" + studyRevisionId +
                '}';
    }
}
