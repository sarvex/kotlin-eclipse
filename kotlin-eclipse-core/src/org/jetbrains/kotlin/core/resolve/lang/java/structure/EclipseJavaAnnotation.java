/*******************************************************************************
 * Copyright 2000-2014 JetBrains s.r.o.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.jetbrains.kotlin.core.resolve.lang.java.structure;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.resolve.java.structure.JavaAnnotation;
import org.jetbrains.jet.lang.resolve.java.structure.JavaAnnotationArgument;
import org.jetbrains.jet.lang.resolve.java.structure.JavaClass;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

import com.google.common.collect.Lists;

public class EclipseJavaAnnotation extends EclipseJavaElement<IAnnotationBinding> implements JavaAnnotation {

    protected EclipseJavaAnnotation(IAnnotationBinding javaAnnotation) {
        super(javaAnnotation);
    }

    @Override
    @Nullable
    public JavaAnnotationArgument findArgument(@NotNull Name name) {
        for (IMemberValuePairBinding member : getBinding().getDeclaredMemberValuePairs()) {
            if (name.equals(member.getName())) {
                return EclipseJavaAnnotationArgument.create(member.getValue(), name, getJavaProject());
            }
        }
        
        return null;
    }

    @Override
    @NotNull
    public Collection<JavaAnnotationArgument> getArguments() {
        List<JavaAnnotationArgument> arguments = Lists.newArrayList();
        for (IMemberValuePairBinding memberValuePair : getBinding().getDeclaredMemberValuePairs()) {
            arguments.add(EclipseJavaAnnotationArgument.create(
                    memberValuePair.getValue(), 
                    Name.identifier(memberValuePair.getName()), 
                    getJavaProject()));
        }
        
        return arguments;
    }

    @Override
    @Nullable
    public FqName getFqName() {
        return new FqName(getBinding().getName());
    }

    @Override
    @Nullable
    public JavaClass resolve() {
        return new EclipseJavaClass(getBinding().getAnnotationType());
    }

}