/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ElementModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;

import java.util.*;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.mockito.Mockito.*;

/**
 * Created by Nikolay Groshkov on 14-Jun-17.
 */
public class ModelTreeIteratorTest {

    private CompositeModelNode compositeModelNode;
    private CompositeModelNode compositeModelNodeForExceptions;
    private Iterator<ModelNode> iterator;

    @Before
    @SuppressWarnings("unchecked")
    public void prepare() {
        compositeModelNode = mock(CompositeModelNode.class, CALLS_REAL_METHODS);
        when(compositeModelNode.getUuid()).thenReturn("uuid");
        when(compositeModelNode.getName()).thenReturn("name");
        compositeModelNode.setSubNodes(new LinkedList());


        compositeModelNodeForExceptions = mock(CompositeModelNode.class, CALLS_REAL_METHODS);
        when(compositeModelNodeForExceptions.getUuid()).thenReturn("uuid");
        when(compositeModelNodeForExceptions.getName()).thenReturn("name");
        when(compositeModelNodeForExceptions.getSubNodes()).thenReturn(Collections.emptyList());
    }

    @After
    public void cleanup() {
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testModelTreeIterator() {
        // test hasNext on an empty SubNodes (returns one element)
        iterator = compositeModelNode.treeIterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(compositeModelNode, iterator.next());
        Assert.assertFalse(iterator.hasNext());
        Assert.assertFalse(iterator.hasNext());

        // test with a collection with several items, make sure the iterator goes through each item, in the correct order (if there is one)
        ModelNode modelNode1 = new ElementModel();
        modelNode1.setName("modelNode1");


        CompositeModelNode modelNode2 = mock(CompositeModelNode.class, CALLS_REAL_METHODS);
        modelNode2.setName("modelNode2");
        modelNode2.setUuid("uuid2");
        modelNode2.setSubNodes(new LinkedList());

        ModelNode modelNode21 = new ElementModel();
        modelNode21.setName("modelNode21");
        modelNode21.setUuid("uuid21");

        ModelNode modelNode22 = new ElementModel();
        modelNode22.setName("modelNode22");
        modelNode22.setUuid("uuid22");

        ModelNode modelNode23 = new ElementModel();
        modelNode23.setName("modelNode23");
        modelNode23.setUuid("uuid23");

        modelNode2.addSubNode(modelNode21);
        modelNode2.addSubNode(modelNode22);
        modelNode2.addSubNode(modelNode23);
        compositeModelNode.addSubNode(modelNode1);
        compositeModelNode.addSubNode(modelNode2);

        List<ModelNode> modelNodes = new ArrayList(Arrays.asList(compositeModelNode, modelNode1, modelNode2,
                modelNode21, modelNode22, modelNode23));

        iterator = compositeModelNode.treeIterator();
        ModelNode currentModelNode;

        Assert.assertTrue(iterator.hasNext());

        currentModelNode = iterator.next();
        Assert.assertThat(modelNodes, hasItem(currentModelNode));
        modelNodes.remove(currentModelNode);
        Assert.assertEquals(5, modelNodes.size());
        Assert.assertTrue(iterator.hasNext());

        currentModelNode = iterator.next();
        Assert.assertThat(modelNodes, hasItem(currentModelNode));
        modelNodes.remove(currentModelNode);
        Assert.assertEquals(4, modelNodes.size());
        Assert.assertTrue(iterator.hasNext());

        currentModelNode = iterator.next();
        Assert.assertThat(modelNodes, hasItem(currentModelNode));
        modelNodes.remove(currentModelNode);
        Assert.assertEquals(3, modelNodes.size());
        Assert.assertTrue(iterator.hasNext());

        currentModelNode = iterator.next();
        Assert.assertThat(modelNodes, hasItem(currentModelNode));
        modelNodes.remove(currentModelNode);
        Assert.assertEquals(2, modelNodes.size());
        Assert.assertTrue(iterator.hasNext());

        currentModelNode = iterator.next();
        Assert.assertThat(modelNodes, hasItem(currentModelNode));
        modelNodes.remove(currentModelNode);
        Assert.assertEquals(1, modelNodes.size());
        Assert.assertTrue(iterator.hasNext());

        currentModelNode = iterator.next();
        Assert.assertThat(modelNodes, hasItem(currentModelNode));
        modelNodes.remove(currentModelNode);
        Assert.assertEquals(0, modelNodes.size());
        Assert.assertFalse(iterator.hasNext());
    }

    // test next() on an empty collection (throws exception)
    @Test(expected = NoSuchElementException.class)
    @SuppressWarnings("unchecked")
    public void testNoSuchElementException() {
        iterator = compositeModelNodeForExceptions.treeIterator();
        iterator.next();
        iterator.next();
    }

    // test remove on that collection: UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    @SuppressWarnings("unchecked")
    public void testRemove() {
        iterator = compositeModelNodeForExceptions.treeIterator();
        iterator.remove();
    }


}
