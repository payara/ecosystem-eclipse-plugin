/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.sdk.utils.LinkedList;
import org.eclipse.payara.tools.sdk.utils.StringPrefixTree;

/**
 * Content to verify on server administration command execution standard output and data to send on
 * standard input.
 * <p/>
 *
 * @author Tomas Kraus
 */
public class ProcessIOContent {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Prefix tree final node content.
     */
    protected static class TreeNode {
        /** Match array index. */
        private final Short index;

        /** Response result. */
        private final ProcessIOResult result;

        /**
         * Creates an instance of prefix tree final node content.
         * <p/>
         *
         * @param index Match array index.
         * @param result Response result.
         */
        private TreeNode(final Short index, final ProcessIOResult result) {
            this.index = index;
            this.result = result;
        }
    }

    /**
     * Abstract input or output token.
     */
    protected abstract static class Token {

        /**
         * Array of match indicators for individual success input strings. This value shall not be null.
         */
        private final boolean matchSuccess[];

        /**
         * Array of match indicators for individual error input strings. This value shall not be null.
         */
        private final boolean matchError[];

        /** Strings to be matched on standard output stored in prefix tree. */
        private final StringPrefixTree<TreeNode> outputStrings;

        /** Maximum length of all stored strings. */
        private final int maxLen;

        /**
         * Create an instance of abstract token expecting set of strings on standard output.
         * <p/>
         *
         * @param inputSuccess Array of input strings considered as successful response to be matched before
         * prompt.
         * @param inputError Array of input strings considered as error response to be matched before
         * prompt.
         */
        protected Token(final String[] inputSuccess, final String[] inputError) {
            int lenSuccess = inputSuccess != null ? inputSuccess.length : 0;
            int lenError = inputError != null ? inputError.length : 0;
            int maxLenLocal = 0;
            matchSuccess = new boolean[lenSuccess];
            matchError = new boolean[lenError];
            outputStrings = new StringPrefixTree<>(false);
            for (short i = 0; i < lenSuccess; i++) {
                matchSuccess[i] = false;
                outputStrings.add(inputSuccess[i],
                        new TreeNode(i, ProcessIOResult.SUCCESS));
                if (inputSuccess[i].length() > maxLenLocal) {
                    maxLenLocal = inputSuccess[i].length();
                }
            }
            for (short i = 0; i < lenError; i++) {
                matchError[i] = false;
                outputStrings.add(inputError[i],
                        new TreeNode(i, ProcessIOResult.ERROR));
                if (inputError[i].length() > maxLenLocal) {
                    maxLenLocal = inputError[i].length();
                }
            }
            maxLen = maxLenLocal;
        }

        /**
         * Get process input prompt different from global input prompt.
         * <p/>
         *
         * @return Always returns <code>null</code>.
         */
        protected String getPrompt() {
            return null;
        }

        /**
         * Returns status of success matching.
         * <p/>
         *
         * @return Value of <code>true</code> when all successful input strings were matched or
         * <coe>false</code> otherwise.
         */
        protected boolean isSuccess() {
            boolean success = true;
            for (boolean matchSucces : matchSuccess) {
                success = success && matchSucces;
            }
            return success;
        }

        /**
         * Array of match indicators for individual error input strings. This value shall not be null.
         *
         * @return the matchError
         */
        protected boolean[] getMatchError() {
            return matchError;
        }

        /**
         * Get strings to be matched on standard output stored in prefix tree.
         * <p/>
         *
         * @return Strings to be matched on standard output stored in prefix tree.
         */
        protected StringPrefixTree<TreeNode> getOutputStrings() {
            return outputStrings;
        }

        /**
         * Get maximum length of all stored strings.
         * <p/>
         *
         * @return Maximum length of all stored strings.
         */
        protected int getMaxLen() {
            return maxLen;
        }

        /**
         * Search for tokens in provided string.
         * <p/>
         *
         * @param str String to be compared with stored tokens.
         * @param offset Beginning index for searching.
         * @return Search result.
         */
        protected ProcessIOResult match(CharSequence str, int offset) {
            TreeNode node = outputStrings.prefixMatch(str, offset);
            if (node != null) {
                switch (node.result) {
                case SUCCESS:
                    matchSuccess[node.index] = true;
                    return isSuccess()
                            ? ProcessIOResult.SUCCESS
                            : ProcessIOResult.UNKNOWN;
                case ERROR:
                    matchError[node.index] = true;
                    return ProcessIOResult.ERROR;
                }
            }
            return ProcessIOResult.UNKNOWN;
        }
    }

    /**
     * Output token contains data to be send on prompt.
     */
    protected static class OutputToken extends Token {

        /**
         * Create an instance of output token expecting set of strings on standard output.
         * <p/>
         *
         * @param inputSuccess Array of input strings considered as successful response to be matched before
         * prompt.
         * @param inputError Array of input strings considered as error response to be matched before
         * prompt.
         */
        protected OutputToken(final String[] inputSuccess, final String[] inputError) {
            super(inputSuccess, inputError);
        }

    }

    /**
     * Input token contains data to be send on prompt.
     */
    protected static class InputToken extends Token {

        /** Process input prompt when differs from global input prompt. */
        private final String prompt;

        /**
         * Create an instance of input token expecting custom input prompt and set of strings on standard
         * output.
         * <p/>
         *
         * @param prompt Process input prompt different from global input prompt.
         * @param input Array of input strings to be matched before prompt.
         * @param output Output to be sent after all input strings are matched and prompt is received.
         */
        protected InputToken(final String prompt, final String[] input,
                final String output) {
            super(input, null);
            this.prompt = prompt;
        }

        /**
         * Get process input prompt different from global input prompt.
         * <p/>
         *
         * @return Process input prompt different from global input prompt or <code>null</code> when no such
         * prompt is set.
         */
        @Override
        protected String getPrompt() {
            return prompt;
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ProcessIOContent.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Process input prompt. Data on standard input are expected after sending this string to standard
     * output.
     */
    private final String prompt;

    /** List of tokens to be processed. */
    private final LinkedList<Token> tokens;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of server administration command execution content.
     * <p/>
     *
     * @param prompt Process input prompt.
     */
    public ProcessIOContent(final String prompt) {
        this.prompt = prompt;
        tokens = new LinkedList<>();
    }

    /**
     * Creates an instance of server administration command execution content.
     * <p/>
     * No input prompt is set. This constructor may be used for commands which are expecting no input.
     */
    public ProcessIOContent() {
        this(null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get global process input prompt.
     * <p/>
     *
     * @return Global process input prompt.
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Get current input prompt.
     * <p/>
     *
     * @return Current input prompt.
     */
    public String getCurrentPrompt() {
        Token token = tokens.getCurrent();
        String tokenPrompt = token != null ? token.getPrompt() : null;
        return tokenPrompt != null ? tokenPrompt : prompt;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add next expected process output to be matched.
     * <p/>
     * All strings in <code>inputSuccess</code> array must be matched for successful evaluation. Any
     * matching string from <code>inputError</code> will cause evaluation as error.
     * <p/>
     *
     * @param inputSuccess Array of input strings considered as successful response to be matched before
     * prompt.
     * @param inputError Array of input strings considered as error response to be matched before
     * prompt.
     */
    public void addOutput(final String[] inputSuccess,
            final String[] inputError) {
        tokens.addLast(new OutputToken(inputSuccess, inputError));
    }

    /**
     * Add next expected process output to be matched.
     * <p/>
     * All strings in <code>inputSuccess</code> array must be matched for successful evaluation.
     * <p/>
     *
     * @param inputSuccess Array of input strings considered as successful response to be matched before
     * prompt.
     */
    public void addOutput(final String[] inputSuccess) {
        tokens.addLast(new OutputToken(inputSuccess, null));
    }

    /**
     * Add next expected process input dependent on matched process output.
     * <p/>
     * All strings in <code>inputSuccess</code> array must be matched for successful evaluation. Any
     * matching string from <code>inputError</code> will cause evaluation as error.
     * <p/>
     *
     * @param prompt Process input prompt different from global input prompt.
     * @param input Array of input strings to be matched before prompt.
     * @param output Output to be sent after all input strings are matched and prompt is received.
     */
    public void addInput(final String prompt, final String[] input,
            final String output) {
        tokens.addLast(new InputToken(prompt, input, output));
    }

    /**
     * Add next expected process input dependent on matched process output.
     * <p/>
     * <code>inputSuccess</code> string must be matched for successful evaluation. Any matching string
     * from <code>inputError</code> will cause evaluation as error.
     * <p/>
     *
     * @param prompt Process input prompt different from global input prompt.
     * @param input Input string to be matched before prompt.
     * @param output Output to be sent after all input strings are matched and prompt is received.
     */
    public void addInput(final String prompt, final String input,
            final String output) {
        tokens.addLast(new InputToken(prompt, new String[] { input }, output));
    }

    /**
     * Add next expected process input dependent on matched process output.
     * <p/>
     * All strings in <code>inputSuccess</code> array must be matched for successful evaluation. Any
     * matching string from <code>inputError</code> will cause evaluation as error.
     * <p/>
     *
     * @param input Array of input strings to be matched before prompt.
     * @param output Output to be sent after all input strings are matched and prompt is received.
     */
    public void addInput(final String[] input, final String output) {
        tokens.addLast(new InputToken(prompt, input, output));
    }

    /**
     * Add next expected process input dependent on matched process output.
     * <p/>
     * <code>inputSuccess</code> string must be matched for successful evaluation. Any matching string
     * from <code>inputError</code> will cause evaluation as error.
     * <p/>
     *
     * @param input Input string to be matched before prompt.
     * @param output Output to be sent after all input strings are matched and prompt is received.
     */
    public void addInput(final String input, final String output) {
        tokens.addLast(new InputToken(prompt, new String[] { input }, output));
    }

    /**
     * Get first token to be processed from list.
     * <p/>
     *
     * @return First token from list or <code>null</code> when list is empty.
     */
    public Token firstToken() {
        tokens.first();
        return tokens.getCurrent();
    }

    /**
     * Get next token to be processed from list.
     * <p/>
     * Next token from list or <code>null</code> when there are no more tokens in the list.
     */
    public Token nextToken() {
        return tokens.next() ? tokens.getCurrent() : null;
    }

}
