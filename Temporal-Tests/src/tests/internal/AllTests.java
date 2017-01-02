/*******************************************************************************
 * Copyright (c) 2011-2012 Oracle. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 and Eclipse Distribution License v. 1.0 which accompanies
 * this distribution. The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution
 * License is available at http://www.eclipse.org/org/documents/edl-v10.php.
 * 
 * Contributors: dclarke - Bug 361016: Future Versions Examples
 ******************************************************************************/
package tests.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Internal test suite verifying how EclipseLInk and the temporal extensions are working.
 *
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
@RunWith(Suite.class)
@SuiteClasses({ VerifyConfigTests.class, 
                VerifySchemaManager.class,
                TemporalHelperTests.class, 
                TemporalEntityManagerTests.class, 
                TemporalEntityTests.class,
                //WrapperPolicyTests.class,
                })
public class AllTests {
}
