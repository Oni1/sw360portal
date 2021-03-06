/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.siemens.sw360.moderation.db;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.ModerationState;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.moderation.DocumentType;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.siemens.sw360.datahandler.common.CommonUtils.notEmptyOrNull;

/**
 * Class for accessing the CouchDB database for the moderation objects
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class ModerationDatabaseHandler {

    private static final Logger log = Logger.getLogger(ModerationDatabaseHandler.class);

    /**
     * Connection to the couchDB database
     */
    private final ModerationRequestRepository repository;

    public ModerationDatabaseHandler(String url, String dbName) throws MalformedURLException {
        DatabaseConnector db = new DatabaseConnector(url, dbName);

        // Create the repository
        repository = new ModerationRequestRepository(db);
    }

    public List<ModerationRequest> getRequestsByModerator(String moderator) {
        return repository.getRequestsByModerator(moderator);
    }

    public List<ModerationRequest> getRequestsByRequestingUser(String user) {
        return repository.getRequestsByRequestingUser(user);
    }

    public ModerationRequest getRequest(String requestId) {
        return repository.get(requestId);
    }

    public List<ModerationRequest> getRequestByDocumentId(String documentId) {
        List<ModerationRequest> requests = CommonUtils.nullToEmptyList(repository.getRequestsByDocumentId(documentId));

        Collections.sort(requests, CommonUtils.compareByTimeStampDescending());

        return requests;
    }


    public void updateModerationRequest(ModerationRequest request) {
        repository.update(request);
    }


    public void deleteRequestsOnDocument(String documentId) {
        List<ModerationRequest> requests = repository.getRequestsByDocumentId(documentId);

        if (requests != null) {
            if (requests.size() > 1) {
                log.warn("More than one moderation request found for document " + documentId);
            }

            for (ModerationRequest request : requests) {
                repository.remove(request);
            }
        }
    }

    public void refuseRequest(String requestId) {
        ModerationRequest request = repository.get(requestId);
        request.moderationState = ModerationState.REJECTED;
        repository.update(request);
    }


    public void createRequest(Component component, String user, Boolean isDeleteRequest) {
        // Define moderators
        Set<String> moderators = new HashSet<>();
        CommonUtils.add(moderators, component.getCreatedBy());

        ModerationRequest request = createStubRequest(user, isDeleteRequest, component.getId(), moderators);

        // Set meta-data
        request.setDocumentType(DocumentType.COMPONENT);
        request.setDocumentName(SW360Utils.printName(component));

        // Set the object TODO 10
        component.unsetReleaseIds();
        component.unsetReleases();

        request.setComponent(component);

        addOrUpdate(request);
    }

    public void createRequest(Release release, String user, Boolean isDeleteRequest) {
        // Define moderators
        Set<String> moderators = new HashSet<>();
        CommonUtils.add(moderators, release.getCreatedBy());
        CommonUtils.addAll(moderators, release.getModerators());

        ModerationRequest request = createStubRequest(user, isDeleteRequest, release.getId(), moderators);

        // Set meta-data
        request.setDocumentType(DocumentType.RELEASE);
        request.setDocumentName(SW360Utils.printName(release));

        // Set the object
        SW360Utils.setVendorId(release);
        request.setRelease(release);

        addOrUpdate(request);
    }

    public void createRequest(Project project, String user, Boolean isDeleteRequest) {
        // Define moderators
        Set<String> moderators = new HashSet<>();
        CommonUtils.add(moderators, project.getCreatedBy());
        CommonUtils.add(moderators, project.getLeadArchitect());
        CommonUtils.add(moderators, project.getProjectResponsible());
        CommonUtils.addAll(moderators, project.getModerators());
        CommonUtils.addAll(moderators, project.getComoderators());

        ModerationRequest request = createStubRequest(user, isDeleteRequest, project.getId(), moderators);

        // Set meta-data
        request.setDocumentType(DocumentType.PROJECT);
        request.setDocumentName(SW360Utils.printName(project));

        // Set the object
        request.setProject(project);

        addOrUpdate(request);
    }

    public void addOrUpdate(ModerationRequest request) {
        if (request.isSetId()) {
            repository.update(request);
        } else {
            repository.add(request);
        }
    }

    private ModerationRequest createStubRequest(String user, boolean isDeleteRequest, String documentId, Set<String> moderators) {
        final ModerationRequest request;

        List<ModerationRequest> requestByDocumentId = getRequestByDocumentId(documentId);
        Optional<ModerationRequest> firstModerationRequestOfUser = CommonUtils.getFirstModerationRequestOfUser(requestByDocumentId, user);
        if (firstModerationRequestOfUser.isPresent() && CommonUtils.isStillRelevant(firstModerationRequestOfUser.get())) {
            request = firstModerationRequestOfUser.get();
        } else {
            request = new ModerationRequest();
            request.setRequestingUser(user);
            request.setDocumentId(documentId);
        }

        request.setTimestamp(System.currentTimeMillis());
        request.setModerationState(ModerationState.PENDING);
        request.setRequestDocumentDelete(isDeleteRequest);
        request.setModerators(Sets.filter(moderators, notEmptyOrNull()));

        return request;

    }


}
