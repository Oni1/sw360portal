<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ This program is free software; you can redistribute it and/or modify it under
  ~ the terms of the GNU General Public License Version 2.0 as published by the
  ~ Free Software Foundation with classpath exception.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  ~ FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
  ~ more details.
  ~
  ~ You should have received a copy of the GNU General Public License along with
  ~ this program (please see the COPYING file); if not, write to the Free
  ~ Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  ~ 02110-1301, USA.
  --%>
<%@include file="/html/init.jsp" %>
<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>


<portlet:defineObjects/>
<liferay-theme:defineObjects/>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>

<jsp:useBean id="component" class="com.siemens.sw360.datahandler.thrift.components.Component" scope="request"/>
<jsp:useBean id="selectedTab" class="java.lang.String" scope="request"/>
<jsp:useBean id="usingProjects" type="java.util.Set<com.siemens.sw360.datahandler.thrift.projects.Project>" scope="request"/>

<jsp:useBean id="usingComponents" type="java.util.Set<com.siemens.sw360.datahandler.thrift.components.Component>" scope="request"/>

<jsp:useBean id="documentType" class="java.lang.String" scope="request"/>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>

<jsp:include page="/html/utils/includes/attachmentsDelete.jsp"/>

<portlet:resourceURL var="subscribeComponentURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.SUBSCRIBE%>"/>
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
</portlet:resourceURL>


<portlet:resourceURL var="unsubscribeComponentURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.UNSUBSCRIBE%>"/>
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
</portlet:resourceURL>

<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Component: ${component.name}</span> <input type="button"
                                                                                                 onclick="editComponent()"
                                                                                                 id="edit" value="Edit"
                                                                                                 class="addButton">
    <sw360:DisplaySubscribeButton email="<%=themeDisplay.getUser().getEmailAddress()%>" object="${component}"
                                  id="SubscribeButton" onclick="subscribeComponent('SubscribeButton')"  altonclick="unsubscribeComponent('SubscribeButton')" />
</p>
<div id="content">
    <div class="container-fluid">
        <div id="myTab" class="row-fluid">
            <ul class="nav nav-tabs span2">
                <li <core_rt:if test="${selectedTab == 'Summary' || empty selectedTab}"> class="active" </core_rt:if> ><a href="#tab-Summary">Summary</a></li>
                <li <core_rt:if test="${selectedTab == 'Clearing'}"> class="active" </core_rt:if>><a href="#tab-ClearingStatus">Release Overview</a></li>
                <li <core_rt:if test="${selectedTab == 'Attachments'}"> class="active" </core_rt:if>><a href="#tab-Attachments">Attachments</a></li>
                <li <core_rt:if test="${selectedTab == 'Wiki'}"> class="active" </core_rt:if>><a href="#tab-Wiki">Wiki</a></li>
            </ul>
            <div class="tab-content span10">
                <div id="tab-Summary" class="tab-pane">
                    <%@include file="/html/components/includes/components/summary.jspf" %>
                    <%@include file="/html/components/includes/components/usingDocuments.jspf" %>
                </div>
                <div id="tab-ClearingStatus">
                    <%@include file="/html/components/includes/components/clearingStatus.jspf" %>
                </div>
                <div id="tab-Attachments">
                    <jsp:include page="/html/utils/includes/attachmentsDetail.jsp" />
                </div>
                <div id="tab-Wiki" class="tab-pane">
                    <%@include file="/html/components/includes/wiki.jsp" %>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    var tabView;
    var Y = YUI().use(
            'aui-tabview',
            function (Y) {
                tabView = new Y.TabView(
                        {
                            srcNode: '#myTab',
                            stacked: true,
                            type: 'tab'
                        }
                ).render();
            }
    );

    function editComponent() {
        window.location ='<portlet:renderURL ><portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/><portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/></portlet:renderURL>'
    }

    function doAjax(url,spanId , successCallback) {
        var $resultElement = $('#' + spanId);
        $resultElement.val("...");
        $.ajax({
            type: 'POST',
            url: url,
            success: function (data) {
                successCallback(spanId, data);
            },
            error: function () {
                $resultElement.val("error");
            }
        });
    }

    function subscribed(spanId, data) {
        var $resultElement = $('#' + spanId);
        var msg = data.result == "SUCCESS" ? "Unsubscribe" : data.result;
        $resultElement.val(msg);
        $resultElement.addClass("subscribed");
        $resultElement.attr("onclick","unsubscribeComponent('"+spanId+"')");
    }

    function unsubscribed(spanId, data) {
        var $resultElement = $('#' + spanId);
        var msg = data.result == "SUCCESS" ? "Subscribe" : data.result;
        $resultElement.val(msg);
        $resultElement.removeClass("subscribed");
        $resultElement.attr("onclick","subscribeComponent('"+spanId+"')");
    }

    function subscribeComponent(spanId) {
        doAjax('<%=subscribeComponentURL%>',spanId , subscribed);
    }

    function unsubscribeComponent(spanId) {
        doAjax('<%=unsubscribeComponentURL%>',spanId , unsubscribed);
    }

</script>
