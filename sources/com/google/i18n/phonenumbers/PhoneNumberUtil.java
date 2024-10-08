package com.google.i18n.phonenumbers;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber$PhoneNumber;
import com.google.i18n.phonenumbers.internal.MatcherApi;
import com.google.i18n.phonenumbers.internal.RegexBasedMatcher;
import com.google.i18n.phonenumbers.internal.RegexCache;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberUtil {
    private static final Map<Character, Character> ALL_PLUS_NUMBER_GROUPING_SYMBOLS;
    private static final Map<Character, Character> ALPHA_MAPPINGS;
    private static final Map<Character, Character> ALPHA_PHONE_MAPPINGS;
    private static final Pattern CAPTURING_DIGIT_PATTERN = Pattern.compile("(\\p{Nd})");
    private static final Map<Character, Character> DIALLABLE_CHAR_MAPPINGS;
    private static final Pattern EXTN_PATTERN;
    static final String EXTN_PATTERNS_FOR_MATCHING = createExtnPattern("xｘ#＃~～");
    private static final String EXTN_PATTERNS_FOR_PARSING;
    private static final Pattern FIRST_GROUP_ONLY_PREFIX_PATTERN = Pattern.compile("\\(?\\$1\\)?");
    private static final Pattern FIRST_GROUP_PATTERN = Pattern.compile("(\\$\\d)");
    private static final Set<Integer> GEO_MOBILE_COUNTRIES;
    private static final Set<Integer> GEO_MOBILE_COUNTRIES_WITHOUT_MOBILE_AREA_CODES;
    private static final Map<Integer, String> MOBILE_TOKEN_MAPPINGS;
    static final Pattern NON_DIGITS_PATTERN = Pattern.compile("(\\D+)");
    static final Pattern PLUS_CHARS_PATTERN = Pattern.compile("[+＋]+");
    static final Pattern SECOND_NUMBER_START_PATTERN = Pattern.compile("[\\\\/] *x");
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[-x‐-―−ー－-／  ­​⁠　()（）［］.\\[\\]/~⁓∼～]+");
    private static final Pattern SINGLE_INTERNATIONAL_PREFIX = Pattern.compile("[\\d]+(?:[~⁓∼～][\\d]+)?");
    static final Pattern UNWANTED_END_CHAR_PATTERN = Pattern.compile("[[\\P{N}&&\\P{L}]&&[^#]]+$");
    private static final String VALID_ALPHA;
    private static final Pattern VALID_ALPHA_PHONE_PATTERN = Pattern.compile("(?:.*?[A-Za-z]){3}.*");
    private static final String VALID_PHONE_NUMBER;
    private static final Pattern VALID_PHONE_NUMBER_PATTERN;
    private static final Pattern VALID_START_CHAR_PATTERN = Pattern.compile("[+＋\\p{Nd}]");
    private static PhoneNumberUtil instance = null;
    private static final Logger logger = Logger.getLogger(PhoneNumberUtil.class.getName());
    private final Map<Integer, List<String>> countryCallingCodeToRegionCodeMap;
    private final Set<Integer> countryCodesForNonGeographicalRegion = new HashSet();
    private final MatcherApi matcherApi = RegexBasedMatcher.create();
    private final MetadataSource metadataSource;
    private final Set<String> nanpaRegions = new HashSet(35);
    private final RegexCache regexCache = new RegexCache(100);
    private final Set<String> supportedRegions = new HashSet(320);

    public enum MatchType {
        NOT_A_NUMBER,
        NO_MATCH,
        SHORT_NSN_MATCH,
        NSN_MATCH,
        EXACT_MATCH
    }

    public enum PhoneNumberFormat {
        E164,
        INTERNATIONAL,
        NATIONAL,
        RFC3966
    }

    public enum PhoneNumberType {
        FIXED_LINE,
        MOBILE,
        FIXED_LINE_OR_MOBILE,
        TOLL_FREE,
        PREMIUM_RATE,
        SHARED_COST,
        VOIP,
        PERSONAL_NUMBER,
        PAGER,
        UAN,
        VOICEMAIL,
        UNKNOWN
    }

    public enum ValidationResult {
        IS_POSSIBLE,
        IS_POSSIBLE_LOCAL_ONLY,
        INVALID_COUNTRY_CODE,
        TOO_SHORT,
        INVALID_LENGTH,
        TOO_LONG
    }

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(54, "9");
        MOBILE_TOKEN_MAPPINGS = Collections.unmodifiableMap(hashMap);
        HashSet hashSet = new HashSet();
        hashSet.add(86);
        GEO_MOBILE_COUNTRIES_WITHOUT_MOBILE_AREA_CODES = Collections.unmodifiableSet(hashSet);
        HashSet hashSet2 = new HashSet();
        hashSet2.add(52);
        hashSet2.add(54);
        hashSet2.add(55);
        hashSet2.add(62);
        hashSet2.addAll(hashSet);
        GEO_MOBILE_COUNTRIES = Collections.unmodifiableSet(hashSet2);
        HashMap hashMap2 = new HashMap();
        hashMap2.put('0', '0');
        hashMap2.put('1', '1');
        hashMap2.put('2', '2');
        hashMap2.put('3', '3');
        hashMap2.put('4', '4');
        hashMap2.put('5', '5');
        hashMap2.put('6', '6');
        hashMap2.put('7', '7');
        hashMap2.put('8', '8');
        hashMap2.put('9', '9');
        HashMap hashMap3 = new HashMap(40);
        hashMap3.put('A', '2');
        hashMap3.put('B', '2');
        hashMap3.put('C', '2');
        hashMap3.put('D', '3');
        hashMap3.put('E', '3');
        hashMap3.put('F', '3');
        hashMap3.put('G', '4');
        hashMap3.put('H', '4');
        hashMap3.put('I', '4');
        hashMap3.put('J', '5');
        hashMap3.put('K', '5');
        hashMap3.put('L', '5');
        hashMap3.put('M', '6');
        hashMap3.put('N', '6');
        hashMap3.put('O', '6');
        hashMap3.put('P', '7');
        hashMap3.put('Q', '7');
        hashMap3.put('R', '7');
        hashMap3.put('S', '7');
        hashMap3.put('T', '8');
        hashMap3.put('U', '8');
        hashMap3.put('V', '8');
        hashMap3.put('W', '9');
        hashMap3.put('X', '9');
        hashMap3.put('Y', '9');
        hashMap3.put('Z', '9');
        Map<Character, Character> unmodifiableMap = Collections.unmodifiableMap(hashMap3);
        ALPHA_MAPPINGS = unmodifiableMap;
        HashMap hashMap4 = new HashMap(100);
        hashMap4.putAll(unmodifiableMap);
        hashMap4.putAll(hashMap2);
        ALPHA_PHONE_MAPPINGS = Collections.unmodifiableMap(hashMap4);
        HashMap hashMap5 = new HashMap();
        hashMap5.putAll(hashMap2);
        hashMap5.put('+', '+');
        hashMap5.put('*', '*');
        hashMap5.put('#', '#');
        DIALLABLE_CHAR_MAPPINGS = Collections.unmodifiableMap(hashMap5);
        HashMap hashMap6 = new HashMap();
        for (Character charValue : unmodifiableMap.keySet()) {
            char charValue2 = charValue.charValue();
            hashMap6.put(Character.valueOf(Character.toLowerCase(charValue2)), Character.valueOf(charValue2));
            hashMap6.put(Character.valueOf(charValue2), Character.valueOf(charValue2));
        }
        hashMap6.putAll(hashMap2);
        hashMap6.put('-', '-');
        hashMap6.put(65293, '-');
        hashMap6.put(8208, '-');
        hashMap6.put(8209, '-');
        hashMap6.put(8210, '-');
        hashMap6.put(8211, '-');
        hashMap6.put(8212, '-');
        hashMap6.put(8213, '-');
        hashMap6.put(8722, '-');
        hashMap6.put('/', '/');
        hashMap6.put(65295, '/');
        hashMap6.put(' ', ' ');
        hashMap6.put(12288, ' ');
        hashMap6.put(8288, ' ');
        hashMap6.put('.', '.');
        hashMap6.put(65294, '.');
        ALL_PLUS_NUMBER_GROUPING_SYMBOLS = Collections.unmodifiableMap(hashMap6);
        StringBuilder sb = new StringBuilder();
        Map<Character, Character> map = ALPHA_MAPPINGS;
        sb.append(Arrays.toString(map.keySet().toArray()).replaceAll("[, \\[\\]]", ""));
        sb.append(Arrays.toString(map.keySet().toArray()).toLowerCase().replaceAll("[, \\[\\]]", ""));
        String sb2 = sb.toString();
        VALID_ALPHA = sb2;
        String str = "\\p{Nd}{2}|[+＋]*+(?:[-x‐-―−ー－-／  ­​⁠　()（）［］.\\[\\]/~⁓∼～*]*\\p{Nd}){3,}[-x‐-―−ー－-／  ­​⁠　()（）［］.\\[\\]/~⁓∼～*" + sb2 + "\\p{Nd}" + "]*";
        VALID_PHONE_NUMBER = str;
        String createExtnPattern = createExtnPattern(",;" + "xｘ#＃~～");
        EXTN_PATTERNS_FOR_PARSING = createExtnPattern;
        EXTN_PATTERN = Pattern.compile("(?:" + createExtnPattern + ")$", 66);
        VALID_PHONE_NUMBER_PATTERN = Pattern.compile(str + "(?:" + createExtnPattern + ")?", 66);
    }

    private static String createExtnPattern(String str) {
        return ";ext=(\\p{Nd}{1,7})|[  \\t,]*(?:e?xt(?:ensi(?:ó?|ó))?n?|ｅ?ｘｔｎ?|доб|[" + str + "]|int|anexo|ｉｎｔ)[:\\.．]?[  \\t,-]*" + "(\\p{Nd}{1,7})" + "#?|[- ]+(" + "\\p{Nd}" + "{1,5})#";
    }

    PhoneNumberUtil(MetadataSource metadataSource2, Map<Integer, List<String>> map) {
        this.metadataSource = metadataSource2;
        this.countryCallingCodeToRegionCodeMap = map;
        for (Map.Entry next : map.entrySet()) {
            List list = (List) next.getValue();
            if (list.size() != 1 || !"001".equals(list.get(0))) {
                this.supportedRegions.addAll(list);
            } else {
                this.countryCodesForNonGeographicalRegion.add(next.getKey());
            }
        }
        if (this.supportedRegions.remove("001")) {
            logger.log(Level.WARNING, "invalid metadata (country calling code was mapped to the non-geo entity as well as specific region(s))");
        }
        this.nanpaRegions.addAll(map.get(1));
    }

    static CharSequence extractPossibleNumber(CharSequence charSequence) {
        Matcher matcher = VALID_START_CHAR_PATTERN.matcher(charSequence);
        if (!matcher.find()) {
            return "";
        }
        CharSequence subSequence = charSequence.subSequence(matcher.start(), charSequence.length());
        Matcher matcher2 = UNWANTED_END_CHAR_PATTERN.matcher(subSequence);
        if (matcher2.find()) {
            subSequence = subSequence.subSequence(0, matcher2.start());
        }
        Matcher matcher3 = SECOND_NUMBER_START_PATTERN.matcher(subSequence);
        return matcher3.find() ? subSequence.subSequence(0, matcher3.start()) : subSequence;
    }

    static boolean isViablePhoneNumber(CharSequence charSequence) {
        if (charSequence.length() < 2) {
            return false;
        }
        return VALID_PHONE_NUMBER_PATTERN.matcher(charSequence).matches();
    }

    static StringBuilder normalize(StringBuilder sb) {
        if (VALID_ALPHA_PHONE_PATTERN.matcher(sb).matches()) {
            sb.replace(0, sb.length(), normalizeHelper(sb, ALPHA_PHONE_MAPPINGS, true));
        } else {
            sb.replace(0, sb.length(), normalizeDigitsOnly(sb));
        }
        return sb;
    }

    public static String normalizeDigitsOnly(CharSequence charSequence) {
        return normalizeDigits(charSequence, false).toString();
    }

    static StringBuilder normalizeDigits(CharSequence charSequence, boolean z) {
        StringBuilder sb = new StringBuilder(charSequence.length());
        for (int i = 0; i < charSequence.length(); i++) {
            char charAt = charSequence.charAt(i);
            int digit = Character.digit(charAt, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (z) {
                sb.append(charAt);
            }
        }
        return sb;
    }

    private static String normalizeHelper(CharSequence charSequence, Map<Character, Character> map, boolean z) {
        StringBuilder sb = new StringBuilder(charSequence.length());
        for (int i = 0; i < charSequence.length(); i++) {
            char charAt = charSequence.charAt(i);
            Character ch = map.get(Character.valueOf(Character.toUpperCase(charAt)));
            if (ch != null) {
                sb.append(ch);
            } else if (!z) {
                sb.append(charAt);
            }
        }
        return sb.toString();
    }

    static synchronized void setInstance(PhoneNumberUtil phoneNumberUtil) {
        synchronized (PhoneNumberUtil.class) {
            instance = phoneNumberUtil;
        }
    }

    private static boolean descHasPossibleNumberData(Phonemetadata$PhoneNumberDesc phonemetadata$PhoneNumberDesc) {
        return (phonemetadata$PhoneNumberDesc.getPossibleLengthCount() == 1 && phonemetadata$PhoneNumberDesc.getPossibleLength(0) == -1) ? false : true;
    }

    public static synchronized PhoneNumberUtil getInstance() {
        PhoneNumberUtil phoneNumberUtil;
        synchronized (PhoneNumberUtil.class) {
            if (instance == null) {
                setInstance(createInstance(MetadataManager.DEFAULT_METADATA_LOADER));
            }
            phoneNumberUtil = instance;
        }
        return phoneNumberUtil;
    }

    public static PhoneNumberUtil createInstance(MetadataLoader metadataLoader) {
        if (metadataLoader != null) {
            return createInstance((MetadataSource) new MultiFileMetadataSourceImpl(metadataLoader));
        }
        throw new IllegalArgumentException("metadataLoader could not be null.");
    }

    private static PhoneNumberUtil createInstance(MetadataSource metadataSource2) {
        if (metadataSource2 != null) {
            return new PhoneNumberUtil(metadataSource2, CountryCodeToRegionCodeMap.getCountryCodeToRegionCodeMap());
        }
        throw new IllegalArgumentException("metadataSource could not be null.");
    }

    private boolean isValidRegionCode(String str) {
        return str != null && this.supportedRegions.contains(str);
    }

    private boolean hasValidCountryCallingCode(int i) {
        return this.countryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(i));
    }

    public String format(Phonenumber$PhoneNumber phonenumber$PhoneNumber, PhoneNumberFormat phoneNumberFormat) {
        if (phonenumber$PhoneNumber.getNationalNumber() == 0 && phonenumber$PhoneNumber.hasRawInput()) {
            String rawInput = phonenumber$PhoneNumber.getRawInput();
            if (rawInput.length() > 0) {
                return rawInput;
            }
        }
        StringBuilder sb = new StringBuilder(20);
        format(phonenumber$PhoneNumber, phoneNumberFormat, sb);
        return sb.toString();
    }

    public void format(Phonenumber$PhoneNumber phonenumber$PhoneNumber, PhoneNumberFormat phoneNumberFormat, StringBuilder sb) {
        sb.setLength(0);
        int countryCode = phonenumber$PhoneNumber.getCountryCode();
        String nationalSignificantNumber = getNationalSignificantNumber(phonenumber$PhoneNumber);
        PhoneNumberFormat phoneNumberFormat2 = PhoneNumberFormat.E164;
        if (phoneNumberFormat == phoneNumberFormat2) {
            sb.append(nationalSignificantNumber);
            prefixNumberWithCountryCallingCode(countryCode, phoneNumberFormat2, sb);
        } else if (!hasValidCountryCallingCode(countryCode)) {
            sb.append(nationalSignificantNumber);
        } else {
            Phonemetadata$PhoneMetadata metadataForRegionOrCallingCode = getMetadataForRegionOrCallingCode(countryCode, getRegionCodeForCountryCode(countryCode));
            sb.append(formatNsn(nationalSignificantNumber, metadataForRegionOrCallingCode, phoneNumberFormat));
            maybeAppendFormattedExtension(phonenumber$PhoneNumber, metadataForRegionOrCallingCode, phoneNumberFormat, sb);
            prefixNumberWithCountryCallingCode(countryCode, phoneNumberFormat, sb);
        }
    }

    private Phonemetadata$PhoneMetadata getMetadataForRegionOrCallingCode(int i, String str) {
        if ("001".equals(str)) {
            return getMetadataForNonGeographicalRegion(i);
        }
        return getMetadataForRegion(str);
    }

    public String getNationalSignificantNumber(Phonenumber$PhoneNumber phonenumber$PhoneNumber) {
        StringBuilder sb = new StringBuilder();
        if (phonenumber$PhoneNumber.isItalianLeadingZero() && phonenumber$PhoneNumber.getNumberOfLeadingZeros() > 0) {
            char[] cArr = new char[phonenumber$PhoneNumber.getNumberOfLeadingZeros()];
            Arrays.fill(cArr, '0');
            sb.append(new String(cArr));
        }
        sb.append(phonenumber$PhoneNumber.getNationalNumber());
        return sb.toString();
    }

    private void prefixNumberWithCountryCallingCode(int i, PhoneNumberFormat phoneNumberFormat, StringBuilder sb) {
        int i2 = AnonymousClass2.$SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat[phoneNumberFormat.ordinal()];
        if (i2 == 1) {
            sb.insert(0, i).insert(0, '+');
        } else if (i2 == 2) {
            sb.insert(0, " ").insert(0, i).insert(0, '+');
        } else if (i2 == 3) {
            sb.insert(0, CmcConstants.E_NUM_SLOT_SPLIT).insert(0, i).insert(0, '+').insert(0, "tel:");
        }
    }

    private String formatNsn(String str, Phonemetadata$PhoneMetadata phonemetadata$PhoneMetadata, PhoneNumberFormat phoneNumberFormat) {
        return formatNsn(str, phonemetadata$PhoneMetadata, phoneNumberFormat, (CharSequence) null);
    }

    private String formatNsn(String str, Phonemetadata$PhoneMetadata phonemetadata$PhoneMetadata, PhoneNumberFormat phoneNumberFormat, CharSequence charSequence) {
        List<Phonemetadata$NumberFormat> list;
        if (phonemetadata$PhoneMetadata.intlNumberFormats().size() == 0 || phoneNumberFormat == PhoneNumberFormat.NATIONAL) {
            list = phonemetadata$PhoneMetadata.numberFormats();
        } else {
            list = phonemetadata$PhoneMetadata.intlNumberFormats();
        }
        Phonemetadata$NumberFormat chooseFormattingPatternForNumber = chooseFormattingPatternForNumber(list, str);
        return chooseFormattingPatternForNumber == null ? str : formatNsnUsingPattern(str, chooseFormattingPatternForNumber, phoneNumberFormat, charSequence);
    }

    /* access modifiers changed from: package-private */
    public Phonemetadata$NumberFormat chooseFormattingPatternForNumber(List<Phonemetadata$NumberFormat> list, String str) {
        for (Phonemetadata$NumberFormat next : list) {
            int leadingDigitsPatternSize = next.leadingDigitsPatternSize();
            if ((leadingDigitsPatternSize == 0 || this.regexCache.getPatternForRegex(next.getLeadingDigitsPattern(leadingDigitsPatternSize - 1)).matcher(str).lookingAt()) && this.regexCache.getPatternForRegex(next.getPattern()).matcher(str).matches()) {
                return next;
            }
        }
        return null;
    }

    private String formatNsnUsingPattern(String str, Phonemetadata$NumberFormat phonemetadata$NumberFormat, PhoneNumberFormat phoneNumberFormat, CharSequence charSequence) {
        String str2;
        String format = phonemetadata$NumberFormat.getFormat();
        Matcher matcher = this.regexCache.getPatternForRegex(phonemetadata$NumberFormat.getPattern()).matcher(str);
        PhoneNumberFormat phoneNumberFormat2 = PhoneNumberFormat.NATIONAL;
        if (phoneNumberFormat != phoneNumberFormat2 || charSequence == null || charSequence.length() <= 0 || phonemetadata$NumberFormat.getDomesticCarrierCodeFormattingRule().length() <= 0) {
            String nationalPrefixFormattingRule = phonemetadata$NumberFormat.getNationalPrefixFormattingRule();
            if (phoneNumberFormat != phoneNumberFormat2 || nationalPrefixFormattingRule == null || nationalPrefixFormattingRule.length() <= 0) {
                str2 = matcher.replaceAll(format);
            } else {
                str2 = matcher.replaceAll(FIRST_GROUP_PATTERN.matcher(format).replaceFirst(nationalPrefixFormattingRule));
            }
        } else {
            str2 = matcher.replaceAll(FIRST_GROUP_PATTERN.matcher(format).replaceFirst(phonemetadata$NumberFormat.getDomesticCarrierCodeFormattingRule().replace(IMessageAttributeInterface.CC, charSequence)));
        }
        if (phoneNumberFormat != PhoneNumberFormat.RFC3966) {
            return str2;
        }
        Matcher matcher2 = SEPARATOR_PATTERN.matcher(str2);
        if (matcher2.lookingAt()) {
            str2 = matcher2.replaceFirst("");
        }
        return matcher2.reset(str2).replaceAll(CmcConstants.E_NUM_SLOT_SPLIT);
    }

    private void maybeAppendFormattedExtension(Phonenumber$PhoneNumber phonenumber$PhoneNumber, Phonemetadata$PhoneMetadata phonemetadata$PhoneMetadata, PhoneNumberFormat phoneNumberFormat, StringBuilder sb) {
        if (phonenumber$PhoneNumber.hasExtension() && phonenumber$PhoneNumber.getExtension().length() > 0) {
            if (phoneNumberFormat == PhoneNumberFormat.RFC3966) {
                sb.append(";ext=");
                sb.append(phonenumber$PhoneNumber.getExtension());
            } else if (phonemetadata$PhoneMetadata.hasPreferredExtnPrefix()) {
                sb.append(phonemetadata$PhoneMetadata.getPreferredExtnPrefix());
                sb.append(phonenumber$PhoneNumber.getExtension());
            } else {
                sb.append(" ext. ");
                sb.append(phonenumber$PhoneNumber.getExtension());
            }
        }
    }

    /* renamed from: com.google.i18n.phonenumbers.PhoneNumberUtil$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat;
        static final /* synthetic */ int[] $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType;
        static final /* synthetic */ int[] $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource;

        /* JADX WARNING: Can't wrap try/catch for region: R(39:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|16|17|18|19|20|21|22|23|24|25|26|(2:27|28)|29|31|32|33|34|35|36|37|38|39|41|42|43|44|45|46|(3:47|48|50)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(40:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|17|18|19|20|21|22|23|24|25|26|(2:27|28)|29|31|32|33|34|35|36|37|38|39|41|42|43|44|45|46|(3:47|48|50)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(43:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|31|32|33|34|35|36|37|38|39|41|42|43|44|45|46|47|48|50) */
        /* JADX WARNING: Can't wrap try/catch for region: R(44:0|(2:1|2)|3|5|6|7|9|10|11|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|31|32|33|34|35|36|37|38|39|41|42|43|44|45|46|47|48|50) */
        /* JADX WARNING: Can't wrap try/catch for region: R(45:0|1|2|3|5|6|7|9|10|11|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|31|32|33|34|35|36|37|38|39|41|42|43|44|45|46|47|48|50) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0033 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x0095 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x009f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x00a9 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00c4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x00ce */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x00d8 */
        static {
            /*
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType[] r0 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType = r0
                r1 = 1
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r2 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.PREMIUM_RATE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r3 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.TOLL_FREE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r4 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.MOBILE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.FIXED_LINE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE     // Catch:{ NoSuchFieldError -> 0x003e }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r6 = 5
                r4[r5] = r6     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.SHARED_COST     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r6 = 6
                r4[r5] = r6     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.VOIP     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r6 = 7
                r4[r5] = r6     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.PERSONAL_NUMBER     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r6 = 8
                r4[r5] = r6     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.PAGER     // Catch:{ NoSuchFieldError -> 0x006c }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r6 = 9
                r4[r5] = r6     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.UAN     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r6 = 10
                r4[r5] = r6     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberType r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.VOICEMAIL     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r6 = 11
                r4[r5] = r6     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberFormat[] r4 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.values()
                int r4 = r4.length
                int[] r4 = new int[r4]
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat = r4
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberFormat r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.E164     // Catch:{ NoSuchFieldError -> 0x0095 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0095 }
                r4[r5] = r1     // Catch:{ NoSuchFieldError -> 0x0095 }
            L_0x0095:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat     // Catch:{ NoSuchFieldError -> 0x009f }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberFormat r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL     // Catch:{ NoSuchFieldError -> 0x009f }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x009f }
                r4[r5] = r0     // Catch:{ NoSuchFieldError -> 0x009f }
            L_0x009f:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat     // Catch:{ NoSuchFieldError -> 0x00a9 }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberFormat r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.RFC3966     // Catch:{ NoSuchFieldError -> 0x00a9 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a9 }
                r4[r5] = r2     // Catch:{ NoSuchFieldError -> 0x00a9 }
            L_0x00a9:
                int[] r4 = $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat     // Catch:{ NoSuchFieldError -> 0x00b3 }
                com.google.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberFormat r5 = com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.NATIONAL     // Catch:{ NoSuchFieldError -> 0x00b3 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b3 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x00b3 }
            L_0x00b3:
                com.google.i18n.phonenumbers.Phonenumber$PhoneNumber$CountryCodeSource[] r4 = com.google.i18n.phonenumbers.Phonenumber$PhoneNumber.CountryCodeSource.values()
                int r4 = r4.length
                int[] r4 = new int[r4]
                $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource = r4
                com.google.i18n.phonenumbers.Phonenumber$PhoneNumber$CountryCodeSource r5 = com.google.i18n.phonenumbers.Phonenumber$PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN     // Catch:{ NoSuchFieldError -> 0x00c4 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c4 }
                r4[r5] = r1     // Catch:{ NoSuchFieldError -> 0x00c4 }
            L_0x00c4:
                int[] r1 = $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource     // Catch:{ NoSuchFieldError -> 0x00ce }
                com.google.i18n.phonenumbers.Phonenumber$PhoneNumber$CountryCodeSource r4 = com.google.i18n.phonenumbers.Phonenumber$PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_IDD     // Catch:{ NoSuchFieldError -> 0x00ce }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x00ce }
                r1[r4] = r0     // Catch:{ NoSuchFieldError -> 0x00ce }
            L_0x00ce:
                int[] r0 = $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.google.i18n.phonenumbers.Phonenumber$PhoneNumber$CountryCodeSource r1 = com.google.i18n.phonenumbers.Phonenumber$PhoneNumber.CountryCodeSource.FROM_NUMBER_WITHOUT_PLUS_SIGN     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource     // Catch:{ NoSuchFieldError -> 0x00e2 }
                com.google.i18n.phonenumbers.Phonenumber$PhoneNumber$CountryCodeSource r1 = com.google.i18n.phonenumbers.Phonenumber$PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY     // Catch:{ NoSuchFieldError -> 0x00e2 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e2 }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x00e2 }
            L_0x00e2:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.i18n.phonenumbers.PhoneNumberUtil.AnonymousClass2.<clinit>():void");
        }
    }

    /* access modifiers changed from: package-private */
    public Phonemetadata$PhoneNumberDesc getNumberDescByType(Phonemetadata$PhoneMetadata phonemetadata$PhoneMetadata, PhoneNumberType phoneNumberType) {
        switch (AnonymousClass2.$SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[phoneNumberType.ordinal()]) {
            case 1:
                return phonemetadata$PhoneMetadata.getPremiumRate();
            case 2:
                return phonemetadata$PhoneMetadata.getTollFree();
            case 3:
                return phonemetadata$PhoneMetadata.getMobile();
            case 4:
            case 5:
                return phonemetadata$PhoneMetadata.getFixedLine();
            case 6:
                return phonemetadata$PhoneMetadata.getSharedCost();
            case 7:
                return phonemetadata$PhoneMetadata.getVoip();
            case 8:
                return phonemetadata$PhoneMetadata.getPersonalNumber();
            case 9:
                return phonemetadata$PhoneMetadata.getPager();
            case 10:
                return phonemetadata$PhoneMetadata.getUan();
            case 11:
                return phonemetadata$PhoneMetadata.getVoicemail();
            default:
                return phonemetadata$PhoneMetadata.getGeneralDesc();
        }
    }

    /* access modifiers changed from: package-private */
    public Phonemetadata$PhoneMetadata getMetadataForRegion(String str) {
        if (!isValidRegionCode(str)) {
            return null;
        }
        return this.metadataSource.getMetadataForRegion(str);
    }

    /* access modifiers changed from: package-private */
    public Phonemetadata$PhoneMetadata getMetadataForNonGeographicalRegion(int i) {
        if (!this.countryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(i))) {
            return null;
        }
        return this.metadataSource.getMetadataForNonGeographicalRegion(i);
    }

    public String getRegionCodeForCountryCode(int i) {
        List list = this.countryCallingCodeToRegionCodeMap.get(Integer.valueOf(i));
        if (list == null) {
            return "ZZ";
        }
        return (String) list.get(0);
    }

    public int getCountryCodeForRegion(String str) {
        if (isValidRegionCode(str)) {
            return getCountryCodeForValidRegion(str);
        }
        Logger logger2 = logger;
        Level level = Level.WARNING;
        StringBuilder sb = new StringBuilder();
        sb.append("Invalid or missing region code (");
        if (str == null) {
            str = "null";
        }
        sb.append(str);
        sb.append(") provided.");
        logger2.log(level, sb.toString());
        return 0;
    }

    private int getCountryCodeForValidRegion(String str) {
        Phonemetadata$PhoneMetadata metadataForRegion = getMetadataForRegion(str);
        if (metadataForRegion != null) {
            return metadataForRegion.getCountryCode();
        }
        throw new IllegalArgumentException("Invalid region code: " + str);
    }

    private ValidationResult testNumberLength(CharSequence charSequence, Phonemetadata$PhoneMetadata phonemetadata$PhoneMetadata) {
        return testNumberLength(charSequence, phonemetadata$PhoneMetadata, PhoneNumberType.UNKNOWN);
    }

    private ValidationResult testNumberLength(CharSequence charSequence, Phonemetadata$PhoneMetadata phonemetadata$PhoneMetadata, PhoneNumberType phoneNumberType) {
        List<Integer> list;
        Phonemetadata$PhoneNumberDesc numberDescByType = getNumberDescByType(phonemetadata$PhoneMetadata, phoneNumberType);
        ArrayList possibleLengthList = numberDescByType.getPossibleLengthList().isEmpty() ? phonemetadata$PhoneMetadata.getGeneralDesc().getPossibleLengthList() : numberDescByType.getPossibleLengthList();
        List<Integer> possibleLengthLocalOnlyList = numberDescByType.getPossibleLengthLocalOnlyList();
        if (phoneNumberType == PhoneNumberType.FIXED_LINE_OR_MOBILE) {
            if (!descHasPossibleNumberData(getNumberDescByType(phonemetadata$PhoneMetadata, PhoneNumberType.FIXED_LINE))) {
                return testNumberLength(charSequence, phonemetadata$PhoneMetadata, PhoneNumberType.MOBILE);
            }
            Phonemetadata$PhoneNumberDesc numberDescByType2 = getNumberDescByType(phonemetadata$PhoneMetadata, PhoneNumberType.MOBILE);
            if (descHasPossibleNumberData(numberDescByType2)) {
                ArrayList arrayList = new ArrayList(possibleLengthList);
                if (numberDescByType2.getPossibleLengthList().size() == 0) {
                    list = phonemetadata$PhoneMetadata.getGeneralDesc().getPossibleLengthList();
                } else {
                    list = numberDescByType2.getPossibleLengthList();
                }
                arrayList.addAll(list);
                Collections.sort(arrayList);
                if (possibleLengthLocalOnlyList.isEmpty()) {
                    possibleLengthLocalOnlyList = numberDescByType2.getPossibleLengthLocalOnlyList();
                } else {
                    ArrayList arrayList2 = new ArrayList(possibleLengthLocalOnlyList);
                    arrayList2.addAll(numberDescByType2.getPossibleLengthLocalOnlyList());
                    Collections.sort(arrayList2);
                    possibleLengthLocalOnlyList = arrayList2;
                }
                possibleLengthList = arrayList;
            }
        }
        if (((Integer) possibleLengthList.get(0)).intValue() == -1) {
            return ValidationResult.INVALID_LENGTH;
        }
        int length = charSequence.length();
        if (possibleLengthLocalOnlyList.contains(Integer.valueOf(length))) {
            return ValidationResult.IS_POSSIBLE_LOCAL_ONLY;
        }
        int intValue = ((Integer) possibleLengthList.get(0)).intValue();
        if (intValue == length) {
            return ValidationResult.IS_POSSIBLE;
        }
        if (intValue > length) {
            return ValidationResult.TOO_SHORT;
        }
        if (((Integer) possibleLengthList.get(possibleLengthList.size() - 1)).intValue() < length) {
            return ValidationResult.TOO_LONG;
        }
        return possibleLengthList.subList(1, possibleLengthList.size()).contains(Integer.valueOf(length)) ? ValidationResult.IS_POSSIBLE : ValidationResult.INVALID_LENGTH;
    }

    public ValidationResult isPossibleNumberWithReason(Phonenumber$PhoneNumber phonenumber$PhoneNumber) {
        return isPossibleNumberForTypeWithReason(phonenumber$PhoneNumber, PhoneNumberType.UNKNOWN);
    }

    public ValidationResult isPossibleNumberForTypeWithReason(Phonenumber$PhoneNumber phonenumber$PhoneNumber, PhoneNumberType phoneNumberType) {
        String nationalSignificantNumber = getNationalSignificantNumber(phonenumber$PhoneNumber);
        int countryCode = phonenumber$PhoneNumber.getCountryCode();
        if (!hasValidCountryCallingCode(countryCode)) {
            return ValidationResult.INVALID_COUNTRY_CODE;
        }
        return testNumberLength(nationalSignificantNumber, getMetadataForRegionOrCallingCode(countryCode, getRegionCodeForCountryCode(countryCode)), phoneNumberType);
    }

    /* access modifiers changed from: package-private */
    public int extractCountryCode(StringBuilder sb, StringBuilder sb2) {
        if (!(sb.length() == 0 || sb.charAt(0) == '0')) {
            int length = sb.length();
            int i = 1;
            while (i <= 3 && i <= length) {
                int parseInt = Integer.parseInt(sb.substring(0, i));
                if (this.countryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(parseInt))) {
                    sb2.append(sb.substring(i));
                    return parseInt;
                }
                i++;
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int maybeExtractCountryCode(CharSequence charSequence, Phonemetadata$PhoneMetadata phonemetadata$PhoneMetadata, StringBuilder sb, boolean z, Phonenumber$PhoneNumber phonenumber$PhoneNumber) throws NumberParseException {
        if (charSequence.length() == 0) {
            return 0;
        }
        StringBuilder sb2 = new StringBuilder(charSequence);
        Phonenumber$PhoneNumber.CountryCodeSource maybeStripInternationalPrefixAndNormalize = maybeStripInternationalPrefixAndNormalize(sb2, phonemetadata$PhoneMetadata != null ? phonemetadata$PhoneMetadata.getInternationalPrefix() : "NonMatch");
        if (z) {
            phonenumber$PhoneNumber.setCountryCodeSource(maybeStripInternationalPrefixAndNormalize);
        }
        if (maybeStripInternationalPrefixAndNormalize == Phonenumber$PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY) {
            if (phonemetadata$PhoneMetadata != null) {
                int countryCode = phonemetadata$PhoneMetadata.getCountryCode();
                String valueOf = String.valueOf(countryCode);
                String sb3 = sb2.toString();
                if (sb3.startsWith(valueOf)) {
                    StringBuilder sb4 = new StringBuilder(sb3.substring(valueOf.length()));
                    Phonemetadata$PhoneNumberDesc generalDesc = phonemetadata$PhoneMetadata.getGeneralDesc();
                    maybeStripNationalPrefixAndCarrierCode(sb4, phonemetadata$PhoneMetadata, (StringBuilder) null);
                    if ((!this.matcherApi.matchNationalNumber(sb2, generalDesc, false) && this.matcherApi.matchNationalNumber(sb4, generalDesc, false)) || testNumberLength(sb2, phonemetadata$PhoneMetadata) == ValidationResult.TOO_LONG) {
                        sb.append(sb4);
                        if (z) {
                            phonenumber$PhoneNumber.setCountryCodeSource(Phonenumber$PhoneNumber.CountryCodeSource.FROM_NUMBER_WITHOUT_PLUS_SIGN);
                        }
                        phonenumber$PhoneNumber.setCountryCode(countryCode);
                        return countryCode;
                    }
                }
            }
            phonenumber$PhoneNumber.setCountryCode(0);
            return 0;
        } else if (sb2.length() > 2) {
            int extractCountryCode = extractCountryCode(sb2, sb);
            if (extractCountryCode != 0) {
                phonenumber$PhoneNumber.setCountryCode(extractCountryCode);
                return extractCountryCode;
            }
            throw new NumberParseException(NumberParseException.ErrorType.INVALID_COUNTRY_CODE, "Country calling code supplied was not recognised.");
        } else {
            throw new NumberParseException(NumberParseException.ErrorType.TOO_SHORT_AFTER_IDD, "Phone number had an IDD, but after this was not long enough to be a viable phone number.");
        }
    }

    private boolean parsePrefixAsIdd(Pattern pattern, StringBuilder sb) {
        Matcher matcher = pattern.matcher(sb);
        if (!matcher.lookingAt()) {
            return false;
        }
        int end = matcher.end();
        Matcher matcher2 = CAPTURING_DIGIT_PATTERN.matcher(sb.substring(end));
        if (matcher2.find() && normalizeDigitsOnly(matcher2.group(1)).equals("0")) {
            return false;
        }
        sb.delete(0, end);
        return true;
    }

    /* access modifiers changed from: package-private */
    public Phonenumber$PhoneNumber.CountryCodeSource maybeStripInternationalPrefixAndNormalize(StringBuilder sb, String str) {
        if (sb.length() == 0) {
            return Phonenumber$PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY;
        }
        Matcher matcher = PLUS_CHARS_PATTERN.matcher(sb);
        if (matcher.lookingAt()) {
            sb.delete(0, matcher.end());
            normalize(sb);
            return Phonenumber$PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN;
        }
        Pattern patternForRegex = this.regexCache.getPatternForRegex(str);
        normalize(sb);
        if (parsePrefixAsIdd(patternForRegex, sb)) {
            return Phonenumber$PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_IDD;
        }
        return Phonenumber$PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY;
    }

    /* access modifiers changed from: package-private */
    public boolean maybeStripNationalPrefixAndCarrierCode(StringBuilder sb, Phonemetadata$PhoneMetadata phonemetadata$PhoneMetadata, StringBuilder sb2) {
        int length = sb.length();
        String nationalPrefixForParsing = phonemetadata$PhoneMetadata.getNationalPrefixForParsing();
        if (!(length == 0 || nationalPrefixForParsing.length() == 0)) {
            Matcher matcher = this.regexCache.getPatternForRegex(nationalPrefixForParsing).matcher(sb);
            if (matcher.lookingAt()) {
                Phonemetadata$PhoneNumberDesc generalDesc = phonemetadata$PhoneMetadata.getGeneralDesc();
                boolean matchNationalNumber = this.matcherApi.matchNationalNumber(sb, generalDesc, false);
                int groupCount = matcher.groupCount();
                String nationalPrefixTransformRule = phonemetadata$PhoneMetadata.getNationalPrefixTransformRule();
                if (nationalPrefixTransformRule != null && nationalPrefixTransformRule.length() != 0 && matcher.group(groupCount) != null) {
                    StringBuilder sb3 = new StringBuilder(sb);
                    sb3.replace(0, length, matcher.replaceFirst(nationalPrefixTransformRule));
                    if (matchNationalNumber && !this.matcherApi.matchNationalNumber(sb3.toString(), generalDesc, false)) {
                        return false;
                    }
                    if (sb2 != null && groupCount > 1) {
                        sb2.append(matcher.group(1));
                    }
                    sb.replace(0, sb.length(), sb3.toString());
                    return true;
                } else if (matchNationalNumber && !this.matcherApi.matchNationalNumber(sb.substring(matcher.end()), generalDesc, false)) {
                    return false;
                } else {
                    if (!(sb2 == null || groupCount <= 0 || matcher.group(groupCount) == null)) {
                        sb2.append(matcher.group(1));
                    }
                    sb.delete(0, matcher.end());
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public String maybeStripExtension(StringBuilder sb) {
        Matcher matcher = EXTN_PATTERN.matcher(sb);
        if (!matcher.find() || !isViablePhoneNumber(sb.substring(0, matcher.start()))) {
            return "";
        }
        int groupCount = matcher.groupCount();
        for (int i = 1; i <= groupCount; i++) {
            if (matcher.group(i) != null) {
                String group = matcher.group(i);
                sb.delete(matcher.start(), sb.length());
                return group;
            }
        }
        return "";
    }

    private boolean checkRegionForParsing(CharSequence charSequence, String str) {
        if (!isValidRegionCode(str)) {
            return (charSequence == null || charSequence.length() == 0 || !PLUS_CHARS_PATTERN.matcher(charSequence).lookingAt()) ? false : true;
        }
        return true;
    }

    public Phonenumber$PhoneNumber parse(CharSequence charSequence, String str) throws NumberParseException {
        Phonenumber$PhoneNumber phonenumber$PhoneNumber = new Phonenumber$PhoneNumber();
        parse(charSequence, str, phonenumber$PhoneNumber);
        return phonenumber$PhoneNumber;
    }

    public void parse(CharSequence charSequence, String str, Phonenumber$PhoneNumber phonenumber$PhoneNumber) throws NumberParseException {
        parseHelper(charSequence, str, false, true, phonenumber$PhoneNumber);
    }

    static void setItalianLeadingZerosForPhoneNumber(CharSequence charSequence, Phonenumber$PhoneNumber phonenumber$PhoneNumber) {
        if (charSequence.length() > 1 && charSequence.charAt(0) == '0') {
            phonenumber$PhoneNumber.setItalianLeadingZero(true);
            int i = 1;
            while (i < charSequence.length() - 1 && charSequence.charAt(i) == '0') {
                i++;
            }
            if (i != 1) {
                phonenumber$PhoneNumber.setNumberOfLeadingZeros(i);
            }
        }
    }

    private void parseHelper(CharSequence charSequence, String str, boolean z, boolean z2, Phonenumber$PhoneNumber phonenumber$PhoneNumber) throws NumberParseException {
        int i;
        if (charSequence == null) {
            throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "The phone number supplied was null.");
        } else if (charSequence.length() <= 250) {
            StringBuilder sb = new StringBuilder();
            String charSequence2 = charSequence.toString();
            buildNationalNumberForParsing(charSequence2, sb);
            if (!isViablePhoneNumber(sb)) {
                throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "The string supplied did not seem to be a phone number.");
            } else if (!z2 || checkRegionForParsing(sb, str)) {
                if (z) {
                    phonenumber$PhoneNumber.setRawInput(charSequence2);
                }
                String maybeStripExtension = maybeStripExtension(sb);
                if (maybeStripExtension.length() > 0) {
                    phonenumber$PhoneNumber.setExtension(maybeStripExtension);
                }
                Phonemetadata$PhoneMetadata metadataForRegion = getMetadataForRegion(str);
                StringBuilder sb2 = new StringBuilder();
                try {
                    i = maybeExtractCountryCode(sb, metadataForRegion, sb2, z, phonenumber$PhoneNumber);
                } catch (NumberParseException e) {
                    Matcher matcher = PLUS_CHARS_PATTERN.matcher(sb);
                    if (e.getErrorType() != NumberParseException.ErrorType.INVALID_COUNTRY_CODE || !matcher.lookingAt()) {
                        throw new NumberParseException(e.getErrorType(), e.getMessage());
                    }
                    i = maybeExtractCountryCode(sb.substring(matcher.end()), metadataForRegion, sb2, z, phonenumber$PhoneNumber);
                    if (i == 0) {
                        throw new NumberParseException(NumberParseException.ErrorType.INVALID_COUNTRY_CODE, "Could not interpret numbers after plus-sign.");
                    }
                }
                if (i != 0) {
                    String regionCodeForCountryCode = getRegionCodeForCountryCode(i);
                    if (!regionCodeForCountryCode.equals(str)) {
                        metadataForRegion = getMetadataForRegionOrCallingCode(i, regionCodeForCountryCode);
                    }
                } else {
                    sb2.append(normalize(sb));
                    if (str != null) {
                        phonenumber$PhoneNumber.setCountryCode(metadataForRegion.getCountryCode());
                    } else if (z) {
                        phonenumber$PhoneNumber.clearCountryCodeSource();
                    }
                }
                if (sb2.length() >= 2) {
                    if (metadataForRegion != null) {
                        StringBuilder sb3 = new StringBuilder();
                        StringBuilder sb4 = new StringBuilder(sb2);
                        maybeStripNationalPrefixAndCarrierCode(sb4, metadataForRegion, sb3);
                        ValidationResult testNumberLength = testNumberLength(sb4, metadataForRegion);
                        if (!(testNumberLength == ValidationResult.TOO_SHORT || testNumberLength == ValidationResult.IS_POSSIBLE_LOCAL_ONLY || testNumberLength == ValidationResult.INVALID_LENGTH)) {
                            if (z && sb3.length() > 0) {
                                phonenumber$PhoneNumber.setPreferredDomesticCarrierCode(sb3.toString());
                            }
                            sb2 = sb4;
                        }
                    }
                    int length = sb2.length();
                    if (length < 2) {
                        throw new NumberParseException(NumberParseException.ErrorType.TOO_SHORT_NSN, "The string supplied is too short to be a phone number.");
                    } else if (length <= 17) {
                        setItalianLeadingZerosForPhoneNumber(sb2, phonenumber$PhoneNumber);
                        phonenumber$PhoneNumber.setNationalNumber(Long.parseLong(sb2.toString()));
                    } else {
                        throw new NumberParseException(NumberParseException.ErrorType.TOO_LONG, "The string supplied is too long to be a phone number.");
                    }
                } else {
                    throw new NumberParseException(NumberParseException.ErrorType.TOO_SHORT_NSN, "The string supplied is too short to be a phone number.");
                }
            } else {
                throw new NumberParseException(NumberParseException.ErrorType.INVALID_COUNTRY_CODE, "Missing or invalid default region.");
            }
        } else {
            throw new NumberParseException(NumberParseException.ErrorType.TOO_LONG, "The string supplied was too long to parse.");
        }
    }

    private void buildNationalNumberForParsing(String str, StringBuilder sb) {
        int indexOf = str.indexOf(";phone-context=");
        if (indexOf >= 0) {
            int i = indexOf + 15;
            if (i < str.length() - 1 && str.charAt(i) == '+') {
                int indexOf2 = str.indexOf(59, i);
                if (indexOf2 > 0) {
                    sb.append(str.substring(i, indexOf2));
                } else {
                    sb.append(str.substring(i));
                }
            }
            int indexOf3 = str.indexOf("tel:");
            sb.append(str.substring(indexOf3 >= 0 ? indexOf3 + 4 : 0, indexOf));
        } else {
            sb.append(extractPossibleNumber(str));
        }
        int indexOf4 = sb.indexOf(";isub=");
        if (indexOf4 > 0) {
            sb.delete(indexOf4, sb.length());
        }
    }

    private static Phonenumber$PhoneNumber copyCoreFieldsOnly(Phonenumber$PhoneNumber phonenumber$PhoneNumber) {
        Phonenumber$PhoneNumber phonenumber$PhoneNumber2 = new Phonenumber$PhoneNumber();
        phonenumber$PhoneNumber2.setCountryCode(phonenumber$PhoneNumber.getCountryCode());
        phonenumber$PhoneNumber2.setNationalNumber(phonenumber$PhoneNumber.getNationalNumber());
        if (phonenumber$PhoneNumber.getExtension().length() > 0) {
            phonenumber$PhoneNumber2.setExtension(phonenumber$PhoneNumber.getExtension());
        }
        if (phonenumber$PhoneNumber.isItalianLeadingZero()) {
            phonenumber$PhoneNumber2.setItalianLeadingZero(true);
            phonenumber$PhoneNumber2.setNumberOfLeadingZeros(phonenumber$PhoneNumber.getNumberOfLeadingZeros());
        }
        return phonenumber$PhoneNumber2;
    }

    public MatchType isNumberMatch(Phonenumber$PhoneNumber phonenumber$PhoneNumber, Phonenumber$PhoneNumber phonenumber$PhoneNumber2) {
        Phonenumber$PhoneNumber copyCoreFieldsOnly = copyCoreFieldsOnly(phonenumber$PhoneNumber);
        Phonenumber$PhoneNumber copyCoreFieldsOnly2 = copyCoreFieldsOnly(phonenumber$PhoneNumber2);
        if (copyCoreFieldsOnly.hasExtension() && copyCoreFieldsOnly2.hasExtension() && !copyCoreFieldsOnly.getExtension().equals(copyCoreFieldsOnly2.getExtension())) {
            return MatchType.NO_MATCH;
        }
        int countryCode = copyCoreFieldsOnly.getCountryCode();
        int countryCode2 = copyCoreFieldsOnly2.getCountryCode();
        if (countryCode == 0 || countryCode2 == 0) {
            copyCoreFieldsOnly.setCountryCode(countryCode2);
            if (copyCoreFieldsOnly.exactlySameAs(copyCoreFieldsOnly2)) {
                return MatchType.NSN_MATCH;
            }
            if (isNationalNumberSuffixOfTheOther(copyCoreFieldsOnly, copyCoreFieldsOnly2)) {
                return MatchType.SHORT_NSN_MATCH;
            }
            return MatchType.NO_MATCH;
        } else if (copyCoreFieldsOnly.exactlySameAs(copyCoreFieldsOnly2)) {
            return MatchType.EXACT_MATCH;
        } else {
            if (countryCode != countryCode2 || !isNationalNumberSuffixOfTheOther(copyCoreFieldsOnly, copyCoreFieldsOnly2)) {
                return MatchType.NO_MATCH;
            }
            return MatchType.SHORT_NSN_MATCH;
        }
    }

    private boolean isNationalNumberSuffixOfTheOther(Phonenumber$PhoneNumber phonenumber$PhoneNumber, Phonenumber$PhoneNumber phonenumber$PhoneNumber2) {
        String valueOf = String.valueOf(phonenumber$PhoneNumber.getNationalNumber());
        String valueOf2 = String.valueOf(phonenumber$PhoneNumber2.getNationalNumber());
        return valueOf.endsWith(valueOf2) || valueOf2.endsWith(valueOf);
    }
}
