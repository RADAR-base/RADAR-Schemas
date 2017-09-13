
/*
 * Copyright 2017 King's College London and The Hyve
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

package org.radarcns.schema.validation.roles;

import org.radarcns.schema.validation.ValidationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * TODO.
 */
public interface Validator<T> extends Function<T, Collection<ValidationException>> {
    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static <T> Validator<T> validate(Predicate<T> predicate, String message) {
        return object -> predicate.test(object) ? Collections.emptySet()
                : Collections.singleton(new ValidationException(message));
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static <T> Validator<T> validate(Predicate<T> predicate, Function<T, String> message) {
        return object -> predicate.test(object)
                ? Collections.emptySet()
                : Collections.singleton(new ValidationException(message.apply(object)));
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static <T> Validator<T> validateNonNull(Predicate<T> predicate, String message) {
        return validate(o -> o != null && predicate.test(o), message);
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static <T, V> Validator<T> validateNonNull(Function<T, V> property, Predicate<V> predicate,
            Function<T, String> message) {
        return validate(o -> {
            V val = property.apply(o);
            return val != null && predicate.test(val);
        }, message);
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static <T, V> Validator<T> validateNonNull(Function<T, V> property, Predicate<V> predicate,
            String message) {
        return validate(o -> {
            V val = property.apply(o);
            return val != null && predicate.test(val);
        }, message);
    }

    /**
     * TODO.
     * @param message TODO
     * @return TODO
     */
    static <T, V> Validator<T> validateNonNull(Function<T, V> property, String message) {
        return validate(o -> property.apply(o) != null, message);
    }

    /**
     * TODO.
     * @param message TODO
     * @return TODO
     */
    static <T, V extends Collection<?>> Validator<T> validateNonEmpty(Function<T, V> property,
            String message) {
        return validate(o -> {
            V val = property.apply(o);
            return val != null && !val.isEmpty();
        }, message);
    }


    /**
     * TODO.
     * @param message TODO
     * @return TODO
     */
    static <T, V extends Collection<?>> Validator<T> validateNonEmpty(Function<T, V> property,
            Function<T, String> message) {
        return validate(o -> {
            V val = property.apply(o);
            return val != null && !val.isEmpty();
        }, message);
    }


    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static <T> Validator<T> validateOrNull(Predicate<T> predicate, String message) {
        return validate(o -> o == null || predicate.test(o), message);
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static <T, V> Validator<T> validateOrNull(Function<T, V> property, Predicate<V> predicate,
            String message) {
        return validate(o -> {
            V val = property.apply(o);
            return val == null || predicate.test(val);
        }, message);
    }

    /**
     * TODO.
     * @param other TODO
     * @return TODO
     */
    default Validator<T> and(Validator<T> other) {
        return object -> {
            List<ValidationException> exceptionList = new ArrayList<>(this.apply(object));
            exceptionList.addAll(other.apply(object));
            return exceptionList;
        };
    }

    static boolean matches(String str, Pattern pattern) {
        return pattern.matcher(str).matches();
    }

    static Predicate<String> matches(Pattern pattern) {
        return str -> pattern.matcher(str).matches();
    }
}
