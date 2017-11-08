/*
 * Copyright 2012, 2014 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.bither.utils;

import net.bither.bitherj.utils.GenericUtils;
import net.bither.bitherj.utils.UnitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class BitcoinURI {
    /**
     * Provides logging for this class
     */
    private static final Logger log = LoggerFactory.getLogger(BitcoinURI.class);

    // Not worth turning into an enum
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_LABEL = "label";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_ADDRESS = "address";
    public static final String FIELD_PAYMENT_REQUEST_URL = "r";

    public static final String BITCOIN_SCHEME = "bitcoin";
    private static final String ENCODED_SPACE_CHARACTER = "%20";
    private static final String AMPERSAND_SEPARATOR = "&";
    private static final String QUESTION_MARK_SEPARATOR = "?";

    /**
     * Contains all the parameters in the order in which they were processed
     */
    private final Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();

    /**
     * Constructs a new BitcoinURI from the given string. Can be for any network.
     *
     * @param input The raw URI data to be parsed (see class comments for accepted formats)
     * @throws net.bither.utils.BitcoinURI.BitcoinURIParseException if the URI is not syntactically or semantically valid.
     */
    public BitcoinURI(String input) throws BitcoinURIParseException {
        checkNotNull(input);

        // Attempt to form the URI (fail fast syntax checking to official standards).
//        URI uri;
//        try {
//            uri = new URI(input);
//        } catch (URISyntaxException e) {
//            throw new BitcoinURIParseException("Bad URI syntax", e);
//        }

        // URI is formed as  bitcoin:<address>?<query parameters>
        // blockchain.info generates URIs of non-BIP compliant form bitcoin://address?....
        // We support both until Ben fixes his code.

        // Remove the bitcoin scheme.
        // (Note: getSchemeSpecificPart() is not used as it unescapes the label and parse then
        // fails.
        // For instance with : bitcoin:129mVqKUmJ9uwPxKJBnNdABbuaaNfho4Ha?amount=0
        // .06&label=Tom%20%26%20Jerry
        // the & (%26) in Tom and Jerry gets interpreted as a separator and the label then gets
        // parsed
        // as 'Tom ' instead of 'Tom & Jerry')
        String schemeSpecificPart;
        if (input.startsWith("bitcoin://")) {
            schemeSpecificPart = input.substring("bitcoin://".length());
        } else if (input.startsWith("bitcoin:")) {
            schemeSpecificPart = input.substring("bitcoin:".length());
        } else {
            throw new BitcoinURIParseException("Unsupported URI scheme");
        }

        // Split off the address from the rest of the query parameters.
        String[] addressSplitTokens = schemeSpecificPart.split("\\?");
        if (addressSplitTokens.length == 0) {
            throw new BitcoinURIParseException("No data found after the bitcoin: prefix");
        }
        String addressToken = addressSplitTokens[0];  // may be empty!

        String[] nameValuePairTokens;
        if (addressSplitTokens.length == 1) {
            // Only an address is specified - use an empty '<name>=<value>' token array.
            nameValuePairTokens = new String[]{};
        } else {
            if (addressSplitTokens.length == 2) {
                // Split into '<name>=<value>' tokens.
                nameValuePairTokens = addressSplitTokens[1].split("&");
            } else {
                throw new BitcoinURIParseException("Too many question marks in URI");
            }
        }

        // Attempt to parse the rest of the URI parameters.
        parseParameters(addressToken, nameValuePairTokens);

        if (!addressToken.isEmpty()) {
            // Attempt to parse the addressToken as a Bitcoin address for this network
            putWithValidation(FIELD_ADDRESS, addressToken);
        }

        if (addressToken.isEmpty() && getPaymentRequestUrl() == null) {
            throw new BitcoinURIParseException("No address and no r= parameter found");
        }
    }

    /**
     * @param nameValuePairTokens The tokens representing the name value pairs (assumed to be
     *                            separated by '=' e.g. 'amount=0.2')
     */
    private void parseParameters(String addressToken, String[] nameValuePairTokens) throws
            BitcoinURIParseException {
        // Attempt to decode the rest of the tokens into a parameter map.
        for (String nameValuePairToken : nameValuePairTokens) {
            final int sepIndex = nameValuePairToken.indexOf('=');
            if (sepIndex == -1) {
                throw new BitcoinURIParseException("Malformed Bitcoin URI - no separator in '" +
                        nameValuePairToken + "'");
            }
            if (sepIndex == 0) {
                throw new BitcoinURIParseException("Malformed Bitcoin URI - empty name '" +
                        nameValuePairToken + "'");
            }
            final String nameToken = nameValuePairToken.substring(0, sepIndex).toLowerCase(Locale
                    .ENGLISH);
            final String valueToken = nameValuePairToken.substring(sepIndex + 1);

            // Parse the amount.
            if (FIELD_AMOUNT.equals(nameToken)) {
                // Decode the amount (contains an optional decimal component to 8dp).
                try {
                    Long amount = Long.valueOf(GenericUtils.toNanoCoins(valueToken, 0).longValue());
                    putWithValidation(FIELD_AMOUNT, amount);
                } catch (NumberFormatException e) {
                    throw new OptionalFieldValidationException(String.format("'%s' is not a " +
                            "valid" + " amount", valueToken), e);
                } catch (ArithmeticException e) {
                    throw new OptionalFieldValidationException(String.format("'%s' has too many "
                            + "decimal places", valueToken), e);
                }
            } else {
                // Known fields and unknown parameters that are optional.
                try {
                    if (valueToken.length() > 0) {
                        putWithValidation(nameToken, URLDecoder.decode(valueToken, "UTF-8"));
                    }
                } catch (UnsupportedEncodingException e) {
                    // Unreachable.
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {
                    if (nameToken != null && !nameToken.equals(FIELD_LABEL)) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        // Note to the future: when you want to implement 'req-expires' have a look at commit
        // 410a53791841
        // which had it in.
    }

    /**
     * Put the value against the key in the map checking for duplication. This avoids address
     * field overwrite etc.
     *
     * @param key   The key for the map
     * @param value The value to store
     */
    private void putWithValidation(String key, Object value) throws BitcoinURIParseException {
        if (parameterMap.containsKey(key)) {
            throw new BitcoinURIParseException(String.format("'%s' is duplicated, " +
                    "" + "URI is invalid", key));
        } else {
            parameterMap.put(key, value);
        }
    }

    /**
     * The Bitcoin Address from the URI, if one was present. It's possible to have Bitcoin URI's
     * with no address if a
     * r= payment protocol parameter is specified, though this form is not recommended as older
     * wallets can't understand
     * it.
     */
    @Nullable
    public String getAddress() {
        return (String) parameterMap.get(FIELD_ADDRESS);
    }

    /**
     * @return The amount name encoded using a pure integer value based at
     * 10,000,000 units is 1 BTC. May be null if no amount is specified
     */
    public long getAmount() {
        Object ob = parameterMap.get(FIELD_AMOUNT);
        if (ob == null) {
            return 0;
        }
        return (Long) ob;
    }

    /**
     * @return The label from the URI.
     */
    public String getLabel() {
        return (String) parameterMap.get(FIELD_LABEL);
    }

    /**
     * @return The message from the URI.
     */
    public String getMessage() {
        return (String) parameterMap.get(FIELD_MESSAGE);
    }

    /**
     * @return The URL where a payment request (as specified in BIP 70) may
     * be fetched.
     */
    public String getPaymentRequestUrl() {
        return (String) parameterMap.get(FIELD_PAYMENT_REQUEST_URL);
    }

    /**
     * @param name The name of the parameter
     * @return The parameter value, or null if not present
     */
    public Object getParameterByName(String name) {
        return parameterMap.get(name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("BitcoinURI[");
        boolean first = true;
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(",");
            }
            builder.append("'").append(entry.getKey()).append("'=").append("'").append(entry
                    .getValue().toString()).append("'");
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Simple Bitcoin URI builder using known good fields.
     *
     * @param address The Bitcoin address
     * @param amount  The amount in nanocoins (decimal)
     * @param label   A label
     * @param message A message
     * @return A String containing the Bitcoin URI
     */
    public static String convertToBitcoinURI(String address, @Nullable long amount,
                                             @Nullable String label, @Nullable String message) {
        checkNotNull(address);
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(BITCOIN_SCHEME).append(":").append(address);

        boolean questionMarkHasBeenOutput = false;

        if (amount != 0) {
            builder.append(QUESTION_MARK_SEPARATOR).append(FIELD_AMOUNT).append("=");
            builder.append(UnitUtil.formatValue(amount, UnitUtil.BitcoinUnit.BTC));
            questionMarkHasBeenOutput = true;
        }

        if (label != null && !"".equals(label)) {
            if (questionMarkHasBeenOutput) {
                builder.append(AMPERSAND_SEPARATOR);
            } else {
                builder.append(QUESTION_MARK_SEPARATOR);
                questionMarkHasBeenOutput = true;
            }
            builder.append(FIELD_LABEL).append("=").append(encodeURLString(label));
        }

        if (message != null && !"".equals(message)) {
            if (questionMarkHasBeenOutput) {
                builder.append(AMPERSAND_SEPARATOR);
            } else {
                builder.append(QUESTION_MARK_SEPARATOR);
            }
            builder.append(FIELD_MESSAGE).append("=").append(encodeURLString(message));
        }

        return builder.toString();
    }

    /**
     * Encode a string using URL encoding
     *
     * @param stringToEncode The string to URL encode
     */
    static String encodeURLString(String stringToEncode) {
        try {
            return java.net.URLEncoder.encode(stringToEncode, "UTF-8").replace("+",
                    ENCODED_SPACE_CHARACTER);
        } catch (UnsupportedEncodingException e) {
            // should not happen - UTF-8 is a valid encoding
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>Exception to provide the following to {@link net.bither.utils.BitcoinURI}:</p>
     * <ul>
     * <li>Provision of parsing error messages</li>
     * </ul>
     * <p>This base exception acts as a general failure mode not attributable to a specific cause
     * (other than
     * that reported in the exception message). Since this is in English,
     * it may not be worth reporting directly
     * to the user other than as part of a "general failure to parse" response.</p>
     */
    public static class BitcoinURIParseException extends Exception {
        public BitcoinURIParseException(String s) {
            super(s);
        }

        public BitcoinURIParseException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }

    public static class OptionalFieldValidationException extends BitcoinURIParseException {

        public OptionalFieldValidationException(String s) {
            super(s);
        }

        public OptionalFieldValidationException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }
}
