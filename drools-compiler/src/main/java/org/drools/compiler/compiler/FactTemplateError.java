/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.drools.compiler.compiler;

import org.drools.drl.ast.descr.BaseDescr;
import org.drools.drl.parser.DroolsError;

public class FactTemplateError extends DroolsError {
    private Package   pkg;
    private BaseDescr descr;
    private Object    object;
    private int[]     line;
    private String    namespace;

    public FactTemplateError(final Package pkg,
                             final BaseDescr descr,
                             final Object object,
                             final String message) {
        super(descr.getResource(), BuilderResultUtils.getProblemMessage( object, message ));
        this.namespace = pkg.getName();
        this.pkg = pkg;
        this.descr = descr;
        this.object = object;
        this.line = new int[] {this.descr.getLine()};
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public Package getPackage() {
        return this.pkg;
    }

    public BaseDescr getDescr() {
        return this.descr;
    }

    public Object getObject() {
        return this.object;
    }
    
    public int[] getLines() {
        return this.line;
    }

    /** 
     * This will return the line number of the error, if possible
     * Otherwise it will be -1
     */
    public int getLine() {
        return this.line[0];
    }


}
