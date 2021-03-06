/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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

import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.siemens.sw360.datahandler.common.CommonUtils;

import javax.portlet.ResourceRequest;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * container for resumable file information
 *
 * @author daniele.fognini@tngtech.com
 */
class ResumableUpload {
    private static final String RESUMABLE_FILENAME = "resumableFilename";
    private static final String RESUMABLE_CHUNK_NUMBER = "resumableChunkNumber";
    private static final String RESUMABLE_CHUNK_SIZE = "resumableChunkSize";
    private static final String RESUMABLE_TOTAL_CHUNKS = "resumableTotalChunks";
    private static final String RESUMABLE_FILE_TYPE = "resumableType";
    private static final String RESUMABLE_UNIQUE_IDENTIFIER = "resumableIdentifier";

    private final String attachmentId;
    private final int chunkNumber;
    private final int chunkSize;
    private final int totalChunks;
    private final String filename;
    private final String fileType;

    private ResumableUpload(String attachmentId, int chunkNumber, int chunkSize, int totalChunks, String filename, String fileType) {
        this.attachmentId = attachmentId;
        this.chunkNumber = chunkNumber;
        this.chunkSize = chunkSize;
        this.totalChunks = totalChunks;
        this.filename = filename;
        this.fileType = fileType;
    }

    public static ResumableUpload from(ResourceRequest request) {
        String attachmentId = request.getParameter(RESUMABLE_UNIQUE_IDENTIFIER);

        int resumableChunkSize = CommonUtils.toUnsignedInt(request.getParameter(RESUMABLE_CHUNK_SIZE));
        int resumableChunkNumber = CommonUtils.toUnsignedInt(request.getParameter(RESUMABLE_CHUNK_NUMBER));
        int resumableTotalChunks = CommonUtils.toUnsignedInt(request.getParameter(RESUMABLE_TOTAL_CHUNKS));

        String resumableFilename = request.getParameter(RESUMABLE_FILENAME);
        String fileType = request.getParameter(RESUMABLE_FILE_TYPE);
        return new ResumableUpload(attachmentId, resumableChunkNumber, resumableChunkSize, resumableTotalChunks, resumableFilename, fileType);
    }

    public static ResumableUpload from(UploadPortletRequest request) {
        String attachmentId = request.getParameter(RESUMABLE_UNIQUE_IDENTIFIER);

        int resumableChunkSize = Integer.valueOf(request.getParameter(RESUMABLE_CHUNK_SIZE));
        int resumableChunkNumber = Integer.valueOf(request.getParameter(RESUMABLE_CHUNK_NUMBER));
        int resumableTotalChunks = Integer.valueOf(request.getParameter(RESUMABLE_TOTAL_CHUNKS));

        String resumableFilename = request.getParameter(RESUMABLE_FILENAME);
        String fileType = request.getParameter(RESUMABLE_FILE_TYPE);
        return new ResumableUpload(attachmentId, resumableChunkNumber, resumableChunkSize, resumableTotalChunks, resumableFilename, fileType);
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public boolean hasAttachmentId() {
        return !isNullOrEmpty(attachmentId) && attachmentId.matches("[a-fA-F0-9]*");
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public String getFilename() {
        return filename;
    }

    public String getFileType() {
        return fileType;
    }

    public boolean isValid() {
        return hasAttachmentId() && isMetaValid();
    }

    public boolean isMetaValid() {
        boolean haveEmptyStrings = isNullOrEmpty(filename);
        boolean haveNonPositiveInts = chunkNumber <= 0 || chunkSize <= 0 || totalChunks <= 0;
        return !haveEmptyStrings && !haveNonPositiveInts;
    }
}
