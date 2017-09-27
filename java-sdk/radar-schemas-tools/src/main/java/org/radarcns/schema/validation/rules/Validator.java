
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

package org.radarcns.schema.validation.rules;

import org.radarcns.schema.validation.ValidationException;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * TODO.
 */
public interface Validator<T> extends Function<T, Stream<ValidationException>> {
    static Stream<ValidationException> check(boolean test, String message) {
        return test ? valid() : raise(message);
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static <T> Validator<T> validate(Predicate<T> predicate, String message) {
        return object -> check(predicate.test(object), message);
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static <T> Validator<T> validate(Predicate<T> predicate, Function<T, String> message) {
        return object -> check(predicate.test(object), message.apply(object));
    }

    static <T, V> Validator<T> validate(Function<T, V> property, Predicate<V> predicate,
            Function<T, String> message) {
        return object -> check(predicate.test(property.apply(object)), message.apply(object));
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
    static <T> Validator<T> validateNonEmpty(Function<T, String> property,
            Function<T, String> message, Validator<String> validator) {
        return o -> {
            String val = property.apply(o);
            if (val == null || val.isEmpty()) {
                return raise(message.apply(o));
            }
            return validator.apply(val);
        };
    }

    /**
     * TODO.
     * @param message TODO
     * @return TODO
     */
    static <T> Validator<T> validateNonEmpty(Function<T, String> property, String message,
            Validator<String> validator) {
        return o -> {
            String val = property.apply(o);
            if (val == null || val.isEmpty()) {
                return raise(message);
            }
            return validator.apply(val);
        };
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
        return object -> Stream.concat(this.apply(object), other.apply(object));
    }

    /**
     * TODO.
     * @param other TODO
     * @return TODO
     */
    default <R> Validator<T> and(Validator<R> other, Function<T, R> toOther) {
        return object -> Stream.concat(this.apply(object), other.apply(toOther.apply(object)));
    }

    static boolean matches(String str, Pattern pattern) {
        return pattern.matcher(str).matches();
    }

    static Predicate<String> matches(Pattern pattern) {
        return str -> pattern.matcher(str).matches();
    }

    static Stream<ValidationException> raise(String message) {
        return Stream.of(new ValidationException(message));
    }

    static Stream<ValidationException> raise(String message, Exception ex) {
        return Stream.of(new ValidationException(message, ex));
    }

    static Stream<ValidationException> valid() {
        return Stream.empty();
    }
}
