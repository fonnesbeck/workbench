package org.pmiops.workbench.cohortreview.querybuilder;

import org.pmiops.workbench.cohortreview.util.PageRequest;
import org.pmiops.workbench.model.PageFilterRequest;
import org.pmiops.workbench.model.PageFilterType;
import org.pmiops.workbench.model.ParticipantData;

public interface ReviewQueryBuilder {

    String NAMED_PARTICIPANTID_PARAM = "participantId";
    String NAMED_DATAID_PARAM = "dataId";

    String getQuery();

    String getDetailsQuery();

    ParticipantData createParticipantData();

    PageRequest createPageRequest(PageFilterRequest request);

    PageFilterType getPageFilterType();
}
