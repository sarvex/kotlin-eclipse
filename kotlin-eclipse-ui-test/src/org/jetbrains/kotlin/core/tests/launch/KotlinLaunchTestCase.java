/*******************************************************************************
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.core.tests.launch;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.jetbrains.kotlin.core.log.KotlinLogger;
import org.jetbrains.kotlin.testframework.editor.KotlinEditorTestCase;
import org.jetbrains.kotlin.ui.launch.KotlinLaunchShortcut;
import org.junit.Assert;

public abstract class KotlinLaunchTestCase extends KotlinEditorTestCase {
    
    public void doTest(String input, String projectName, String packageName, String additionalSrcFolderName) {
        testEditor = configureEditor("Test.kt", input, projectName, packageName);
        try {
            testEditor.getTestJavaProject().addKotlinRuntime();
            
            if (additionalSrcFolderName != null) {
                testEditor.getTestJavaProject().createSourceFolder(additionalSrcFolderName);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        
        launchInForeground();
        Assert.assertNotNull(findOutputConsole());
    }
    
    private IConsole findOutputConsole() {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles = consoleManager.getConsoles();
        IConsole outputConsole = null;
        for (IConsole console : consoles) {
            if (IDebugUIConstants.ID_PROCESS_CONSOLE_TYPE.equals(console.getType())) {
                outputConsole = console;
                break;
            }
        }
        
        return outputConsole;
    }
    
    private void launchInForeground() {
        final ILaunchConfiguration launchConfiguration = KotlinLaunchShortcut.createConfiguration(testEditor.getEditingFile());
        
        try {
            Job job = new WorkspaceJob("test") {
                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    monitor.beginTask("test started", 1);
                    
                    try {
                        launchConfiguration.launch("run", new SubProgressMonitor(monitor, 1), true);
                    } catch (CoreException e) {
                        KotlinLogger.logAndThrow(e);
                        return Status.CANCEL_STATUS;
                    } finally {
                        monitor.done();
                    }
                    
                    return Status.OK_STATUS;
                }
            };
            
            joinBuildThread();
            job.schedule();
            job.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
