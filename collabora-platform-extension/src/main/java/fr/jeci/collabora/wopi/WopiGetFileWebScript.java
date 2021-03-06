/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package fr.jeci.collabora.wopi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.jeci.collabora.alfresco.WOPIAccessTokenInfo;

public class WopiGetFileWebScript extends AbstractWopiWebScript {
	private static final Log logger = LogFactory.getLog(WopiGetFileWebScript.class);

	@Override
	public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException {
		final WOPIAccessTokenInfo wopiToken = wopiToken(req);
		final NodeRef nodeRef = getFileNodeRef(wopiToken);

		if (logger.isDebugEnabled()) {
			logger.debug("WopiGetFile user='" + wopiToken.getUserName() + "' nodeRef='" + nodeRef + "'");
		}

		AuthenticationUtil.pushAuthentication();
		try {
			AuthenticationUtil.setRunAsUser(wopiToken.getUserName());
			final ContentData contentProp = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
			res.setContentType(contentProp.getMimetype());
			res.setContentEncoding(contentProp.getEncoding());

			final ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

			try (InputStream inputStream = reader.getContentInputStream();
					OutputStream outputStream = res.getOutputStream();) {
				IOUtils.copy(inputStream, outputStream);
			}
		} finally {
			AuthenticationUtil.popAuthentication();
		}
	}

}