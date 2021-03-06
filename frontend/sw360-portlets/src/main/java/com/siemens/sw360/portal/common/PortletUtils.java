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
package com.siemens.sw360.portal.common;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.ModerationState;
import com.siemens.sw360.datahandler.thrift.Visibility;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.components.*;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectState;
import com.siemens.sw360.datahandler.thrift.projects.ProjectType;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.meta_data.FieldMetaData;

import javax.portlet.PortletRequest;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.parseInt;

/**
 * Portlet helpers
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class PortletUtils {

    private static final Logger log = Logger.getLogger(PortletUtils.class);

    private PortletUtils() {
        // Utility class with only static functions
    }

    public static ComponentType getComponentTypefromString(String enumNumber) {
        return ComponentType.findByValue(parseInt(enumNumber));
    }

    public static ClearingState getClearingStatefromString(String enumNumber) {
        return ClearingState.findByValue(parseInt(enumNumber));
    }

    public static RepositoryType getRepositoryTypefromString(String enumNumber) {
        return RepositoryType.findByValue(parseInt(enumNumber));
    }

    public static MainlineState getMainlineStatefromString(String enumNumber) {
        return MainlineState.findByValue(parseInt(enumNumber));
    }

    public static ModerationState getModerationStatusfromString(String enumNumber) {
        return ModerationState.findByValue(parseInt(enumNumber));
    }

    public static AttachmentType getAttachmentTypefromString(String enumNumber) {
        return AttachmentType.findByValue(parseInt(enumNumber));
    }

    public static ProjectState getProjectStateFromString(String enumNumber) {
        return ProjectState.findByValue(parseInt(enumNumber));
    }

    public static ProjectType getProjectTypeFromString(String enumNumber) {
        return ProjectType.findByValue(parseInt(enumNumber));
    }
    public static Visibility getVisibilityFromString(String enumNumber) {
        return  Visibility.findByValue(parseInt(enumNumber));
    }

    public static <U extends TFieldIdEnum, T extends TBase<T, U>> void setFieldValue(PortletRequest request, T instance, U field, FieldMetaData fieldMetaData, String prefix) {

        String value = request.getParameter(prefix + field.toString());
        if (value != null) {
            switch (fieldMetaData.valueMetaData.type) {

                case org.apache.thrift.protocol.TType.SET:
                    instance.setFieldValue(field, CommonUtils.splitToSet(value));
                    break;
                case org.apache.thrift.protocol.TType.ENUM:
                    if (!"".equals(value))
                        instance.setFieldValue(field, enumFromString(value, field));
                    break;
                case org.apache.thrift.protocol.TType.I32:
                    if (!"".equals(value))
                        instance.setFieldValue(field, Integer.parseInt(value));
                    break;
                case org.apache.thrift.protocol.TType.BOOL:
                    if (!"".equals(value))
                        instance.setFieldValue(field, true);
                    break;
                default:
                    instance.setFieldValue(field, value);
            }
        }
    }

    public static <U extends TFieldIdEnum> Object enumFromString(String value, U field) {
        if (field == Release._Fields.CLEARING_STATE)
            return getClearingStatefromString(value);
        else if (field == Component._Fields.COMPONENT_TYPE)
            return getComponentTypefromString(value);
        else if (field == Repository._Fields.REPOSITORYTYPE)
            return getRepositoryTypefromString(value);
        else if (field == Release._Fields.MAINLINE_STATE)
            return getMainlineStatefromString(value);
        else if (field == Project._Fields.STATE)
            return getProjectStateFromString(value);
        else if (field == Project._Fields.PROJECT_TYPE)
            return getProjectTypeFromString(value);
        else if (field == Project._Fields.VISBILITY)
            return getVisibilityFromString(value);
        else {
            log.error("Missing case in enumFromString, unknown field was " + field.toString());
            return null;
        }
    }


    public static void updateAttachmentsFromRequest(PortletRequest request, Set<Attachment> attachments) {
        if (attachments == null || attachments.size() == 0) return;

        String[] ids = request.getParameterValues(Release._Fields.ATTACHMENTS.toString() + Attachment._Fields.ATTACHMENT_CONTENT_ID.toString());
        String[] comments = request.getParameterValues(Release._Fields.ATTACHMENTS.toString() + Attachment._Fields.COMMENT.toString());
        String[] atypes = request.getParameterValues(Release._Fields.ATTACHMENTS.toString() + Attachment._Fields.ATTACHMENT_TYPE.toString());

        if (CommonUtils.oneIsNull(atypes, comments, ids)) {
            log.error("We have a problem null arrays");
        } else if (atypes.length != comments.length || atypes.length != ids.length)
            log.error("We have a problem length != other.length ");
        else {
            Map<String, Attachment> attachmentMap = Maps.uniqueIndex(attachments, new Function<Attachment, String>() {
                @Override
                public String apply(Attachment attachment) {
                    return attachment.getAttachmentContentId();
                }
            });

            int length = atypes.length;

            for (int i = 0; i < length; ++i) {

                String id = ids[i];
                if (attachmentMap.containsKey(id)) {

                    Attachment attachment = attachmentMap.get(id);

                    attachment.setComment(comments[i]);
                    attachment.setAttachmentType(getAttachmentTypefromString(atypes[i]));
                } else {
                    log.error("Unable to find attachment!" + id);
                }
            }
        }
    }


    public static Release cloneRelease(String emailFromRequest, Release release) {

        Release newRelease = release.deepCopy();

        //new DB object
        newRelease.unsetId();
        newRelease.unsetRevision();

        //new Owner
        newRelease.setCreatedBy(emailFromRequest);
        newRelease.setCreatedOn(SW360Utils.getCreatedOn());

        //release specifics
        newRelease.unsetCpeid();
        newRelease.unsetAttachments();
        newRelease.unsetClearingInformation();

        return newRelease;
    }

    public static Project cloneProject(String emailFromRequest, String department, Project project) {

        Project newProject = project.deepCopy();

        //new DB object
        newProject.unsetId();
        newProject.unsetRevision();

        //new Owner
        newProject.setCreatedBy(emailFromRequest);
        newProject.setCreatedOn(SW360Utils.getCreatedOn());
        newProject.setBusinessUnit(department);

        //project specifics
        newProject.unsetName();
        newProject.unsetAttachments();

        return newProject;
    }
}
