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
package tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tests.editionsets.FullPersonWithEditionsDelete;
import tests.editionsets.FullPersonWithEditionsMove;

/**
 * Test suite to debug issues with multiple subclasses of {@link FullPersonWithEditions}
 */
@RunWith(Suite.class)
@SuiteClasses({FullPersonWithEditionsQueries.class,
  FullPersonWithEditionsMods.class,
  FullPersonWithEditionsMove.class,
  FullPersonWithEditionsDelete.class})
public class AllFullPersonTests
{
}
