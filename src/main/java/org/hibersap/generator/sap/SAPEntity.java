/*
 * Copyright (C) 2012 akquinet AG
 *
 * This file is part of the Forge Hibersap Plugin.
 *
 * The Forge Hibersap Plugin is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The Forge Hibersap Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with the Forge Hibersap Plugin. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hibersap.generator.sap;

import org.jboss.forge.roaster.model.JavaClass;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;



/**
 * Represents a SAP entity in the hibersap-generator-plugin context
 * <p>
 * Contains a BAPI class an a Set of structure classes if custom types are needed
 * <p>
 * Update using https://github.com/forge/roaster
 *
 * @author Max Schwaab, Ludger Pottmeier
 */
public class SAPEntity {

    /**
     * The BAPI class
     **/
    private final JavaClassSource bapiClass;
    /**
     * The structure classes set
     **/
    private final Set<JavaClassSource> structureClasses = new HashSet<JavaClassSource>();

    /**
     * Constructor - Instantiates a new SAPEntity
     *
     * @param bapiClass - the BAPI class
     */
    public SAPEntity(final JavaClassSource bapiClass) {
        this.bapiClass = bapiClass;
    }

    /**
     * Gets the BAPI class
     *
     * @return the BAPI class
     */
    public JavaClassSource getBapiClass() {
        return this.bapiClass;
    }

    /**
     * Gets the structure classes set
     *
     * @return the structure classes set
     */
    public Set<JavaClassSource> getStructureClasses() {
        return this.structureClasses;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.bapiClass == null) ? 0 : this.bapiClass.hashCode());
        result = prime * result + ((this.structureClasses == null) ? 0 : this.structureClasses.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof SAPEntity) {
            final SAPEntity toCompare = (SAPEntity) object;
            final JavaClass toCompareBapiClass = toCompare.getBapiClass();
            final Set<JavaClassSource> toCompareStructureClasses = toCompare.getStructureClasses();

            return this.bapiClass.equals(toCompareBapiClass) && this.structureClasses.equals(toCompareStructureClasses);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BAPI Class \"" + this.bapiClass.getName() + "\":\n");
        builder.append(this.bapiClass.toString() + "\n\n");
        for (final JavaClassSource structureClass : this.structureClasses) {
            builder.append("Structure Class \"" + structureClass.getName() + "\":\n");
            builder.append(structureClass.toString() + "\n\n");
        }

        return builder.toString();
    }

}
