/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.ui.log;

import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

@SuppressWarnings("restriction")
public class PayaraConsoleTracker implements IPatternMatchListenerDelegate {

    /**
     * The console associated with this line tracker
     */
    private TextConsole payaraConsole;

    @Override
    public void connect(TextConsole console) {
        payaraConsole = console;
    }

    @Override
    public void disconnect() {
        payaraConsole = null;
    }

    @Override
    public void matchFound(PatternMatchEvent event) {
        try {
            int offset = event.getOffset();
            int length = event.getLength();
            IHyperlink link = new JavaStackTraceHyperlink(payaraConsole);
            payaraConsole.addHyperlink(link, offset + 1, length - 2);
        } catch (BadLocationException e) {
            // Ignore
        }
    }

}
