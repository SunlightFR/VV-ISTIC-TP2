# Using PMD

Pick a Java project from Github (see the [instructions](../sujet.md) for suggestions). Run PMD on its source code using any ruleset (see the [pmd install instruction](./pmd-help.md)). Describe below an issue found by PMD that you think should be solved (true positive) and include below the changes you would add to the source code. Describe below an issue found by PMD that is not worth solving (false positive). Explain why you would not solve this issue.

## Answer

J'ai choisi le projet [Apache Commons Math](https://github.com/apache/commons-math).

Avec la règle quickstart, PMD trouve 2264 problèmes dans le projet.
En voici un exemple : 
`commons-math\commons-math-core\src\main\java\org\apache\commons\math4\core\jdkmath\AccurateMath.java:396:	UselessParentheses:	Useless parentheses.` indique qu'à un endroit du code des parenthèses inutiles ont été écrites. Voici le code correspondant :

```java
if (x >= LOG_MAX_VALUE) {
    // Avoid overflow (MATH-905).
    final double t = exp(0.5 * x);
    return (0.5 * t) * t;//ici
} else {
    return 0.5 * exp(x);
}
```

J'ai trouvé un exemple de faux positif avec la règle `category/java/design.xml/AvoidThrowingNullPointerException` : 

`commons-math\commons-math-legacy-exception\src\main\java\org\apache\commons\math4\legacy\exception\NullArgumentException.java:96:	AvoidThrowingNullPointerException:	Avoid throwing null pointer exceptions.`

Voici le code en question :
```java
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math4.legacy.exception;

import org.apache.commons.math4.legacy.exception.util.ExceptionContext;
import org.apache.commons.math4.legacy.exception.util.ExceptionContextProvider;
import org.apache.commons.math4.legacy.exception.util.Localizable;
import org.apache.commons.math4.legacy.exception.util.LocalizedFormats;

/**
 * All conditions checks that fail due to a {@code null} argument must throw
 * this exception.
 * This class is meant to signal a precondition violation ("null is an illegal
 * argument") and so does not extend the standard {@code NullPointerException}.
 * Propagation of {@code NullPointerException} from within Commons-Math is
 * construed to be a bug.
 * <p>
 * Note: from 4.0 onwards, this class extends {@link NullPointerException} instead
 * of {@link MathIllegalArgumentException}.
 *
 * @since 2.2
 */
public class NullArgumentException extends NullPointerException
    implements ExceptionContextProvider {

    /** Serializable version Id. */
    private static final long serialVersionUID = 20150225L;

    /** Context. */
    private final ExceptionContext context;

    /**
     * Default constructor.
     */
    public NullArgumentException() {
        this(LocalizedFormats.NULL_NOT_ALLOWED);
    }
    /**
     * @param pattern Message pattern providing the specific context of
     * the error.
     * @param arguments Values for replacing the placeholders in {@code pattern}.
     */
    public NullArgumentException(Localizable pattern,
                                 Object... arguments) {
        context = new ExceptionContext(this);
        context.addMessage(pattern, arguments);
    }

    /**
     * {@inheritDoc}
     * @since 4.0
     */
    @Override
    public ExceptionContext getContext() {
        return context;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return context.getMessage();
    }

    /** {@inheritDoc} */
    @Override
    public String getLocalizedMessage() {
        return context.getLocalizedMessage();
    }

    /**
     * Checks that an object is not null.
     *
     * @param o Object to be checked.
     * @param pattern Message pattern.
     * @param args Arguments to replace the placeholders in {@code pattern}.
     * @throws NullArgumentException if {@code o} is {@code null}.
     */
    public static void check(Object o,
                             Localizable pattern,
                             Object... args) {
        if (o == null) {
            throw new NullArgumentException(pattern, args);//ici
        }
    }

    /**
     * Checks that an object is not null.
     *
     * @param o Object to be checked.
     * @throws NullArgumentException if {@code o} is {@code null}.
     */
    public static void check(Object o) {
        if (o == null) {
            throw new NullArgumentException();//ici
        }
    }
}
```
Or il s'agit d'une classe qui justement implémente cette exception. Il est donc normal qu'elle l'utilise. Il s'agit donc d'un comportement voulu par les développeurs et non d'une erreur, il ne faut donc pas le corriger.