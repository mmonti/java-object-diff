/*
 * Copyright 2012 Daniel Bechler
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
 */

package de.danielbechler.diff.introspect;

import de.danielbechler.diff.accessor.*;
import de.danielbechler.diff.comparison.*;
import de.danielbechler.diff.mock.*;
import de.danielbechler.diff.path.*;
import de.danielbechler.util.*;
import org.hamcrest.core.*;
import org.testng.annotations.*;

import java.beans.*;

import static org.hamcrest.MatcherAssert.*;

/** @author Daniel Bechler */
public class StandardIntrospectorTest
{
	private StandardIntrospector introspector;

	@BeforeMethod
	public void setUp() throws Exception
	{
		introspector = new StandardIntrospector();
	}

	@Test
	public void testIntrospectWithEqualsOnlyPropertyType() throws Exception
	{
		final Iterable<PropertyAwareAccessor> accessors = introspector.introspect(ObjectWithEqualsOnlyPropertyType.class);
		assertThat(accessors.iterator().hasNext(), Is.is(true));
		final PropertyAwareAccessor propertyAwareAccessor = accessors.iterator().next();
		assertThat(propertyAwareAccessor.getComparisonStrategy(), IsInstanceOf.instanceOf(EqualsOnlyComparisonStrategy.class));
	}

	@Test
	public void testIntrospectWithPropertyAnnotations()
	{
		final Iterable<PropertyAwareAccessor> accessors = introspector.introspect(ObjectWithPropertyAnnotations.class);
		for (final PropertyAwareAccessor accessor : accessors)
		{
			if (accessor.getPathElement().equals(new NamedPropertyElement("ignored")))
			{
				assertThat(accessor.isExcluded(), Is.is(true));
			}
			else if (accessor.getPathElement().equals(new NamedPropertyElement("equalsOnly")))
			{
				assertThat(accessor.getComparisonStrategy(), IsInstanceOf.instanceOf(EqualsOnlyComparisonStrategy.class));
			}
			else if (accessor.getPathElement().equals(new NamedPropertyElement("categorized")))
			{
				assertThat(accessor.getCategories().size(), Is.is(1));
				assertThat(accessor.getCategories(), IsEqual.equalTo(Collections.setOf("foo")));
			}
			else if (accessor.getPathElement().equals(new NamedPropertyElement("item")))
			{
				assertThat(accessor.getComparisonStrategy(), IsNull.nullValue());
				assertThat(accessor.isExcluded(), Is.is(false));
				assertThat(accessor.getCategories().isEmpty(), Is.is(true));
			}
			else if (accessor.getPathElement().equals(new NamedPropertyElement("key")))
			{
				// no op
			}
			else if (accessor.getPathElement().equals(new NamedPropertyElement("value")))
			{
				// no op
			}
			else
			{
				org.testng.Assert.fail("Unexpected accessor: " + accessor.getPathElement());
			}
		}
	}

	@Test
	public void testIntrospectWithInheritedPropertyAnnotations()
	{
		final Iterable<PropertyAwareAccessor> accessors = introspector.introspect(ObjectWithInheritedPropertyAnnotation.class);
		final PropertyAwareAccessor accessor = accessors.iterator().next();
		assertThat((NamedPropertyElement) accessor.getPathElement(), IsEqual.equalTo(new NamedPropertyElement("value")));
		assertThat(accessor.isExcluded(), Is.is(true));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testIntrospectWithNullType() throws Exception
	{
		introspector.introspect(null);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testIntrospectWithSimulatedIntrospectionException() throws Exception
	{
		introspector = new StandardIntrospector()
		{
			@Override
			protected BeanInfo getBeanInfo(final Class<?> type) throws IntrospectionException
			{
				throw new IntrospectionException(type.getCanonicalName());
			}
		};
		introspector.introspect(ObjectWithString.class);
	}
}
