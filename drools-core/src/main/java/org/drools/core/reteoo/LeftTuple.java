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
package org.drools.core.reteoo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.PropagationContext;
import org.drools.core.util.index.TupleList;
import org.kie.api.runtime.rule.FactHandle;

/**
 * A parent class for all specific LeftTuple specializations
 *
 */
public class LeftTuple
        extends AbstractTuple {
    private static final long  serialVersionUID = 540l;

    private int      index;

    private LeftTuple parent;

    // left and right tuples in parent
    private LeftTuple leftParent;

    private RightTuple         rightParent;
    private LeftTuple rightParentPrevious;
    private LeftTuple rightParentNext;

    // children
    private LeftTuple firstChild;
    private LeftTuple lastChild;

    // node memory
    protected TupleList        memory;

    private LeftTuple peer;

    private short              stagedTypeForQueries;

    public LeftTuple() {
        // constructor needed for serialisation
    }

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------
    public LeftTuple(InternalFactHandle factHandle,
                     Sink sink,
                     boolean leftTupleMemoryEnabled) {
        setFactHandle( factHandle );
        setSink(sink);
        if ( leftTupleMemoryEnabled ) {
            factHandle.addTupleInPosition( this );
        }
    }

    public LeftTuple(InternalFactHandle factHandle,
                     LeftTuple leftTuple,
                     Sink sink) {
        setFactHandle( factHandle );
        this.index = leftTuple.getIndex() + 1;
        this.parent = leftTuple.getNextParentWithHandle();
        this.leftParent = leftTuple;
        setSink(sink);
    }

    public LeftTuple(LeftTuple leftTuple,
                     Sink sink,
                     PropagationContext pctx,
                     boolean leftTupleMemoryEnabled) {
        this.index = leftTuple.getIndex() + 1;
        this.parent = leftTuple.getNextParentWithHandle();
        this.leftParent = leftTuple;
        setPropagationContext( pctx );

        if ( leftTupleMemoryEnabled ) {
            if ( leftTuple.getLastChild() != null ) {
                this.handlePrevious = leftTuple.getLastChild();
                this.handlePrevious.setHandleNext( this );
            } else {
                leftTuple.setFirstChild( this );
            }
            leftTuple.setLastChild( this );
        }

        setSink(sink);
    }

    public LeftTuple(LeftTuple leftTuple,
                     RightTuple rightTuple,
                     Sink sink) {
        this.index = leftTuple.getIndex() + 1;
        this.parent = leftTuple.getNextParentWithHandle();
        this.leftParent = leftTuple;
        this.rightParent = rightTuple;

        setFactHandle( rightTuple.getFactHandle() );
        setPropagationContext( rightTuple.getPropagationContext() );

        // insert at the end f the list
        if ( leftTuple.getLastChild() != null ) {
            this.handlePrevious = leftTuple.getLastChild();
            this.handlePrevious.setHandleNext( this );
        } else {
            leftTuple.setFirstChild( this );
        }
        leftTuple.setLastChild( this );

        // insert at the end of the list
        if ( rightTuple.getLastChild() != null ) {
            this.rightParentPrevious = rightTuple.getLastChild();
            this.rightParentPrevious.setRightParentNext( this );
        } else {
            rightTuple.setFirstChild( this );
        }
        rightTuple.setLastChild( this );
        setSink(sink);
    }

    public LeftTuple(LeftTuple leftTuple,
                     RightTuple rightTuple,
                     Sink sink,
                     boolean leftTupleMemoryEnabled) {
        this( leftTuple,
              rightTuple,
              null,
              null,
              sink,
              leftTupleMemoryEnabled );
    }

    public LeftTuple(LeftTuple leftTuple,
                     RightTuple rightTuple,
                     LeftTuple currentLeftChild,
                     LeftTuple currentRightChild,
                     Sink sink,
                     boolean leftTupleMemoryEnabled) {
        setFactHandle( rightTuple.getFactHandle() );
        this.index = leftTuple.getIndex() + 1;
        this.parent = leftTuple.getNextParentWithHandle();
        this.leftParent = leftTuple;
        this.rightParent = rightTuple;
        setPropagationContext( rightTuple.getPropagationContext() );

        if ( leftTupleMemoryEnabled ) {
            if( currentLeftChild == null ) {
                // insert at the end of the list
                if ( leftTuple.getLastChild() != null ) {
                    this.handlePrevious = leftTuple.getLastChild();
                    this.handlePrevious.setHandleNext( this );
                } else {
                    leftTuple.setFirstChild( this );
                }
                leftTuple.setLastChild( this );
            } else {
                // insert before current child
                this.handleNext = currentLeftChild;
                this.handlePrevious = currentLeftChild.getHandlePrevious();
                currentLeftChild.setHandlePrevious( this );
                if( this.handlePrevious == null ) {
                    this.leftParent.setFirstChild( this  );
                } else {
                    this.handlePrevious.setHandleNext( this );
                }
            }

            if( currentRightChild == null ) {
                // insert at the end of the list
                if ( rightTuple.getLastChild() != null ) {
                    this.rightParentPrevious = rightTuple.getLastChild();
                    this.rightParentPrevious.setRightParentNext( this );
                } else {
                    rightTuple.setFirstChild( this );
                }
                rightTuple.setLastChild( this );
            } else {
                // insert before current child
                this.rightParentNext = currentRightChild;
                this.rightParentPrevious = currentRightChild.getRightParentPrevious();
                currentRightChild.setRightParentPrevious( this );
                if( this.rightParentPrevious == null ) {
                    this.rightParent.setFirstChild( this );
                } else {
                    this.rightParentPrevious.setRightParentNext( this );
                }
            }
        }

        setSink(sink);
    }

    public LeftTuple getNextParentWithHandle() {
        // if parent is null, then we are LIAN
        return (handle!=null) ? this : parent != null ? parent.getNextParentWithHandle() : this;
    }

    @Override
    public void reAdd() {
        getFactHandle().addLastLeftTuple( this );
    }

    public void reAddLeft() {
        // The parent can never be the FactHandle (root LeftTuple) as that is handled by reAdd()
        // make sure we aren't already at the end
        if ( this.handleNext != null ) {
            if ( this.handlePrevious != null ) {
                // remove the current LeftTuple from the middle of the chain
                this.handlePrevious.setHandleNext( this.handleNext );
                this.handleNext.setHandlePrevious( this.handlePrevious );
            } else {
                if( this.leftParent.getFirstChild() == this ) {
                    // remove the current LeftTuple from start start of the chain
                    this.leftParent.setFirstChild( getHandleNext() );
                }
                this.handleNext.setHandlePrevious( null );
            }
            // re-add to end
            this.handlePrevious = this.leftParent.getLastChild();
            this.handlePrevious.setHandleNext( this );
            this.leftParent.setLastChild( this );
            this.handleNext = null;
        }
    }

    public void reAddRight() {
        // make sure we aren't already at the end
        if ( this.rightParentNext != null ) {
            if ( this.rightParentPrevious != null ) {
                // remove the current LeftTuple from the middle of the chain
                this.rightParentPrevious.setRightParentNext( this.rightParentNext );
                this.rightParentNext.setRightParentPrevious( this.rightParentPrevious );
            } else {
                if( this.rightParent.getFirstChild() == this ) {
                    // remove the current LeftTuple from the start of the chain
                    this.rightParent.setFirstChild( this.rightParentNext );
                }
                this.rightParentNext.setRightParentPrevious( null );
            }
            // re-add to end
            this.rightParentPrevious = this.rightParent.getLastChild();
            this.rightParentPrevious.setRightParentNext( this );
            this.rightParent.setLastChild( this );
            this.rightParentNext = null;
        }
    }

    @Override
    public void unlinkFromLeftParent() {
        LeftTuple previousParent = getHandlePrevious();
        LeftTuple nextParent = getHandleNext();

        if ( previousParent != null && nextParent != null ) {
            //remove  from middle
            this.handlePrevious.setHandleNext( nextParent );
            this.handleNext.setHandlePrevious( previousParent );
        } else if ( nextParent != null ) {
            //remove from first
            if ( this.leftParent != null ) {
                this.leftParent.setFirstChild( nextParent );
            } else {
                // This is relevant to the root node and only happens at rule removal time
                getFactHandle().removeLeftTuple( this );
            }
            nextParent.setHandlePrevious( null );
        } else if ( previousParent != null ) {
            //remove from end
            if ( this.leftParent != null ) {
                this.leftParent.setLastChild( previousParent );
            } else {
                // relevant to the root node, as here the parent is the FactHandle, only happens at rule removal time
                getFactHandle().removeLeftTuple( this );
            }
            previousParent.setHandleNext( null );
        } else {
            // single remaining item, no previous or next
            if( leftParent != null ) {
                this.leftParent.setFirstChild( null );
                this.leftParent.setLastChild( null );
            } else {
                // it is a root tuple - only happens during rule removal
                getFactHandle().removeLeftTuple( this );
            }
        }

        this.handlePrevious = null;
        this.handleNext = null;
    }

    @Override
    public void unlinkFromRightParent() {
        if ( this.rightParent == null ) {
            // no right parent;
            return;
        }

        LeftTuple previousParent = this.rightParentPrevious;
        LeftTuple nextParent = this.rightParentNext;

        if ( previousParent != null && nextParent != null ) {
            // remove from middle
            this.rightParentPrevious.setRightParentNext( this.rightParentNext );
            this.rightParentNext.setRightParentPrevious( this.rightParentPrevious );
        } else if ( nextParent != null ) {
            // remove from the start
            this.rightParent.setFirstChild( nextParent );
            nextParent.setRightParentPrevious( null );
        } else if ( previousParent != null ) {
            // remove from end
            this.rightParent.setLastChild( previousParent );
            previousParent.setRightParentNext( null );
        } else {
            // single remaining item, no previous or next
            this.rightParent.setFirstChild( null );
            this.rightParent.setLastChild( null );
        }

        this.rightParentPrevious = null;
        this.rightParentNext = null;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    // It's better to always cast to a concrete or abstract class to avoid
    // secondary super cache problem. See https://issues.redhat.com/browse/DROOLS-7521
    public LeftTupleSink getTupleSink() {
        Object sink = getSink();
        if (sink instanceof AccumulateNode) {
            return (AccumulateNode) sink;
        } else if (sink instanceof RuleTerminalNode) {
            return (RuleTerminalNode) sink;
        } else if (sink instanceof RightInputAdapterNode) {
            return (RightInputAdapterNode) sink;
        } else if (sink instanceof ExistsNode) {
            return (ExistsNode) sink;
        }
        return (LeftTupleSink)sink;
    }

    /* Had to add the set method because sink adapters must override
     * the tuple sink set when the tuple was created.
     */
    public void setLeftTupleSink( LeftTupleSink sink ) {
        setSink(sink);
    }

    public LeftTuple getLeftParent() {
        return leftParent;
    }

    public void setLeftParent(LeftTuple leftParent) {
        this.leftParent = leftParent;
    }

    @Override
    public LeftTuple getHandlePrevious() {
        return (LeftTuple) handlePrevious;
    }

    @Override
    public LeftTuple getHandleNext() {
        return (LeftTuple) handleNext;
    }

    public RightTuple getRightParent() {
        return rightParent;
    }

    public void setRightParent(RightTuple rightParent) {
        this.rightParent = rightParent;
    }

    public LeftTuple getRightParentPrevious() {
        return rightParentPrevious;
    }

    public void setRightParentPrevious(LeftTuple rightParentLeft) {
        this.rightParentPrevious = rightParentLeft;
    }

    public LeftTuple getRightParentNext() {
        return rightParentNext;
    }

    public void setRightParentNext(LeftTuple rightParentRight) {
        this.rightParentNext = rightParentRight;
    }

    @Override
    public FactHandle get(int index) {
        LeftTuple entry = this;
        while ( entry.getIndex() != index) {
            entry = entry.getParent();
        }
        return entry.getFactHandle();
    }

    public FactHandle[] toFactHandles() {
        // always use the count of the node that created join (not the sink target)
        FactHandle[] handles = new FactHandle[((LeftTupleSinkNode)getSink()).getLeftTupleSource().getObjectCount()];
        LeftTuple entry = (LeftTuple) skipEmptyHandles();
        for(int i = handles.length-1; i >= 0; i--) {
            handles[i] = entry.getFactHandle();
            entry = entry.getParent();
        }
        return handles;
    }

    public Object[] toObjects(boolean reverse) {
        // always use the count of the node that created join (not the sink target)
        Object[] objs = new Object[((LeftTupleSinkNode)getSink()).getLeftTupleSource().getObjectCount()];
        LeftTuple entry = (LeftTuple) skipEmptyHandles();

        if (!reverse) {
            for (int i = objs.length - 1; i >= 0; i--) {
                objs[i] = entry.getFactHandle().getObject();
                entry = entry.getParent();
            }
        } else {
            for (int i = 0; i < objs.length; i++) {
                objs[i] = entry.getFactHandle().getObject();
                entry = entry.getParent();
            }
        }

        return objs;
    }

    public void clearBlocker() {
        throw new UnsupportedOperationException();
    }

    public void setBlocker(RightTuple blocker) {
        throw new UnsupportedOperationException();
    }

    public RightTuple getBlocker() {
        throw new UnsupportedOperationException();
    }

    public LeftTuple getBlockedPrevious() {
        throw new UnsupportedOperationException();
    }

    public void setBlockedPrevious(LeftTuple blockerPrevious) {
        throw new UnsupportedOperationException();
    }

    public LeftTuple getBlockedNext() {
        throw new UnsupportedOperationException();
    }

    public void setBlockedNext(LeftTuple blockerNext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();

        Tuple entry = skipEmptyHandles();;
        while ( entry != null ) {
            //buffer.append( entry.handle );
            buffer.append(entry.getFactHandle());
            if ( entry.getParent() != null ) {
                buffer.append("\n");
            }
            entry = entry.getParent();
        }
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        return getFactHandle() == null ? 0 : getFactHandle().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof LeftTuple)) {
            return false;
        }

        LeftTuple other = ( (LeftTuple) object );

        // A LeftTuple is  only the same if it has the same hashCode, factId and parent
        if ( this.hashCode() != other.hashCode() || getFactHandle() != other.getFactHandle() ) {
            return false;
        }

        if ( this.parent == null ) {
            return (other.getParent() == null);
        } else {
            return this.parent.equals( other.getParent() );
        }
    }

    @Override
    public int size() {
        return this.index + 1;
    }

    @Override
    public LeftTuple getFirstChild() {
        return firstChild;
    }

    @Override
    public void setFirstChild(LeftTuple firstChild) {
        this.firstChild = firstChild;
    }

    @Override
    public LeftTuple getLastChild() {
        return lastChild;
    }

    @Override
    public void setLastChild(LeftTuple lastChild) {
        this.lastChild = lastChild;
    }

    @Override
    public TupleList getMemory() {
        return this.memory;
    }

    @Override
    public void setMemory(TupleList memory) {
        this.memory = memory;
    }

    @Override
    public LeftTuple getStagedNext() {
        return (LeftTuple) stagedNext;
    }

    @Override
    public LeftTuple getStagedPrevious() {
        return (LeftTuple) stagedPrevious;
    }

    @Override
    public void clearStaged() {
        super.clearStaged();
        if (getContextObject() == Boolean.TRUE) {
            setContextObject( null );
        }
    }

    public LeftTuple getPeer() {
        return peer;
    }

    public void setPeer(LeftTuple peer) {
        this.peer = peer;
    }

    @Override
    public LeftTuple getSubTuple(final int elements) {
        LeftTuple entry = this;
        if ( elements <= this.size() ) {
            final int lastindex = elements - 1;

            while ( entry.getIndex() != lastindex ) {
                // This uses getLeftParent, instead of getParent, as the subnetwork tuple
                // parent could be any node
                entry = entry.getParent();
            }
        }
        return entry;
    }

    @Override
    public LeftTuple getParent() {
        return parent;
    }

    protected String toExternalString() {
        StringBuilder builder = new StringBuilder();
        builder.append( String.format( "%08X", System.identityHashCode( this ) ) ).append( ":" );
        long[] ids = new long[this.index+1];
        Tuple entry = skipEmptyHandles();;
        while( entry != null ) {
            ids[entry.getIndex()] = entry.getFactHandle().getId();
            entry = entry.getParent();
        }
        builder.append( Arrays.toString( ids ) )
               .append( " sink=" )
               .append( this.getSink().getClass().getSimpleName() )
               .append( "(" ).append( getSink().getId() ).append( ")" );
        return  builder.toString();
    }

    @Override
    public void clear() {
        super.clear();
        this.memory = null;
    }

    public void initPeer(LeftTuple original, LeftTupleSink sink) {
        this.index = original.index;
        this.parent = original.parent;
        this.leftParent = original.leftParent;

        setFactHandle( original.getFactHandle() );
        setPropagationContext( original.getPropagationContext() );
        setSink(sink);
    }

    @Override
    public Object getObject(int index) {
        return get(index).getObject();
    }

    @Override
    public ObjectTypeNode.Id getInputOtnId() {
        return getSink() != null ? getTupleSink().getLeftInputOtnId() : null;
    }

    @Override
    public LeftTupleSource getTupleSource() {
        return getSink() != null ? getTupleSink().getLeftTupleSource() : null;
    }

    public short getStagedTypeForQueries() {
        return stagedTypeForQueries;
    }

    public void setStagedTypeForQueries( short stagedTypeForQueries ) {
        this.stagedTypeForQueries = stagedTypeForQueries;
    }

    public boolean isStagedOnRight() {
        return false;
    }

    public Collection<Object> getAccumulatedObjects() {
        if (getFirstChild() == null) {
            return Collections.emptyList();
        }
        Collection<Object> result = new ArrayList<>();
        if ( getContextObject() instanceof AccumulateNode.AccumulateContext ) {
            for (LeftTuple child = getFirstChild(); child != null; child = child.getHandleNext()) {
                result.add(child.getContextObject());
            }
        }
        if ( getFirstChild().getRightParent() instanceof SubnetworkTuple ) {
            LeftTuple leftParent = (( SubnetworkTuple ) getFirstChild().getRightParent()).getLeftParent();
            result.addAll( leftParent.getAccumulatedObjects() );
        }
        return result;
    }
}
