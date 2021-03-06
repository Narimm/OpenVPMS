<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.openvpms.version.Version" %>
<%@ page import="org.openvpms.web.component.subscription.SubscriptionHelper" %>
<%@ page import="org.openvpms.web.echo.button.ShortcutHelper" %>
<%@ page import="org.openvpms.web.resource.i18n.Messages" %>
<%@ page import="org.openvpms.web.system.ServiceHelper" %>
<%--
  ~ Version: 1.0
  ~
  ~ The contents of this file are subject to the OpenVPMS License Version
  ~ 1.0 (the 'License'); you may not use this file except in compliance with
  ~ the License. You may obtain a copy of the License at
  ~ http://www.openvpms.org/license/
  ~
  ~ Software distributed under the License is distributed on an 'AS IS' basis,
  ~ WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  ~ for the specific language governing rights and limitations under the
  ~ License.
  ~
  ~ Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    <title>OpenVPMS</title>
    <style type="text/css">

        body {
            font-family: arial, sans-serif;
            width: 100%;
            height: 100%;
            overflow: hidden;
            margin: 0;
        }

        .border-bl {
            position: absolute;
            background: url(images/border/bottomleft.png) bottom left no-repeat;
            top: 50%;
            left: 50%;
            width: 24em;
            margin-left: -12em; /* ~ half the width of the dialog */
            margin-top: -91px; /* half the height of the dialog */
        }

        .border-br {
            background: url(images/border/bottomright.png) bottom right no-repeat;
        }

        .border-tl {
            background: url(images/border/topleft.png) top left no-repeat;
        }

        .border-l {
            background: url(images/border/left.png) top left repeat-y;
            margin-left: -17px;
            margin-top: 9px;
            margin-bottom: 9px;
        }

        .border-r {
            background: url(images/border/right.png) right repeat-y;
            margin-right: -23px;
        }

        .border-t {
            background: url(images/border/top.png) top left repeat-x;
            padding-top: 8px;
            margin-left: 17px;
            margin-right: 23px;
        }

        .border-tr {
            background: url(images/border/topright.png) top right no-repeat;
        }

        .border-b {
            background: url(images/border/bottom.png) bottom left repeat-x;
            padding-bottom: 14px;
        }

        .header {
            position: absolute;
            background: #99cc66 repeat-x;
            top: 8px;
            left: 8px;
            right: 14px;
            height: 19px;
            color: rgb(255, 255, 255);
            font-size: 16px;
            padding: 5px 10px;
            clear: both;
        }

        .buttons {
            position: absolute;
            background: url(images/buttonrowfill.png) repeat-x;
            left: 8px;
            right: 14px;
            bottom: 14px;
            height: 32px;
        }

        .content {
            text-align: center;
            padding: 20px;
        }

        .label {
            color: rgb(0, 0, 0);
            font-size: 15px;
        }

        .textfield {
            border: 1px inset rgb(240, 230, 140);
            color: rgb(0, 0, 0);
            background-color: rgb(255, 255, 153);
            font-size: 15px;
            width: 10em;
        }

        .button {
            padding: 0 20px;
            height: 25px;
            cursor: pointer;
            border: 1px outset rgb(123, 104, 238);
            color: rgb(0, 0, 0);
            background-color: rgb(153, 204, 102);
            vertical-align: middle;
            font-size: 15px;
        }

        .error {
            background: url(images/error-red.gif) no-repeat;
            padding-left: 32px;
            padding-top: 4px;
            min-height: 24px;
        }

        .footer {
            text-align: center;
            position: absolute;
            bottom: 20px;
            width: 100%;
            overflow: hidden;
        }

        .version {
            font-size: smaller;
            padding-top: 10px;
        }

    </style>

    <script type="text/javascript">
        function onReady() {
            document.getElementById("username").focus();
        }
    </script>

</head>
<body onload='onReady();'>

<form id="loginForm" action="login" method="post">
    <!-- the following divitis puts a shadow border around the login 'dialog' in keeping with the L&F of the rest
         of the app. A basic discussion of the technique can be found here
         http://www.webcredible.co.uk/user-friendly-resources/css/css-round-corners-boxes.shtml
         While cumbersome, it should work on most browsers except IE6, where the absolute positioning of header and
         footer fails, as does the support for transparent PNGs.
         -->

    <div id="dialog" class="border-bl">
        <div class="border-br">
            <div class="border-tl">
                <div class="border-tr">
                    <div class="border-t">
                        <div class="border-b">
                            <div class="border-l">
                                <div class="border-r">
                                    <div class="header"><%= Messages.get("title.login")%>
                                    </div>
                                    <div class="content">
                                        <img style="padding:5px" src="images/openvpms.png" alt="OpenVPMS">

                                        <table style="border-collapse: collapse;">
                                            <tbody>
                                            <tr>
                                                <td align="right" style="padding: 5px;">
                                                    <span class="label"><%= Messages.get("label.username") %></span>
                                                </td>
                                                <td style="padding: 5px;">
                                                    <input id="username" class="textfield" name="username"
                                                           type="text" tabindex="1">
                                                </td>
                                            </tr>
                                            <tr>
                                                <td align="right" style="padding: 5px;">
                                                    <span class="label"><%= Messages.get("label.password") %></span>
                                                </td>
                                                <td style="padding: 5px;">
                                                    <input class="textfield" name="password" type="password"
                                                           tabindex="2">
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                        <div style="min-height:30px">
                                            <%
                                                if ("error".equals(request.getParameter("status"))) {
                                            %>
                                            <div class="error">
                                                <%= Messages.get("login.error")%>
                                            </div>
                                            <%
                                                }
                                            %>
                                        </div>
                                    </div>
                                    <div class="buttons">
                                        <div style="padding: 4px 10px;">
                                            <input tabindex="3" class="button" type="submit"
                                                   value="<%=ShortcutHelper.getText(Messages.get("button.ok"))%>"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</form>
<div class="footer">
    <div style="white-space:pre"><%=SubscriptionHelper.formatSubscription(ServiceHelper.getArchetypeService())%>
    </div>
    <div class="version"><%=Messages.format("openvpms.version", Version.VERSION, Version.REVISION)%>
    </div>
</div>

</body>
</html>
