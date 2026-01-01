package excel.converters;

import excel.converters.ConverterKeyBuild.ConverterKey;
import excel.converters.bigdecimal.BigDecimalBooleanConverter;
import excel.converters.bigdecimal.BigDecimalNumberConverter;
import excel.converters.bigdecimal.BigDecimalStringConverter;
import excel.converters.biginteger.BigIntegerBooleanConverter;
import excel.converters.biginteger.BigIntegerNumberConverter;
import excel.converters.biginteger.BigIntegerStringConverter;
import excel.converters.booleanconverter.BooleanBooleanConverter;
import excel.converters.booleanconverter.BooleanNumberConverter;
import excel.converters.booleanconverter.BooleanStringConverter;
import excel.converters.bytearray.BoxingByteArrayImageConverter;
import excel.converters.bytearray.ByteArrayImageConverter;
import excel.converters.byteconverter.ByteBooleanConverter;
import excel.converters.byteconverter.ByteNumberConverter;
import excel.converters.byteconverter.ByteStringConverter;
import excel.converters.date.DateDateConverter;
import excel.converters.date.DateNumberConverter;
import excel.converters.date.DateStringConverter;
import excel.converters.doubleconverter.DoubleBooleanConverter;
import excel.converters.doubleconverter.DoubleNumberConverter;
import excel.converters.doubleconverter.DoubleStringConverter;
import excel.converters.file.FileImageConverter;
import excel.converters.floatconverter.FloatBooleanConverter;
import excel.converters.floatconverter.FloatNumberConverter;
import excel.converters.floatconverter.FloatStringConverter;
import excel.converters.inputstream.InputStreamImageConverter;
import excel.converters.integer.IntegerBooleanConverter;
import excel.converters.integer.IntegerNumberConverter;
import excel.converters.integer.IntegerStringConverter;
import excel.converters.localdatetime.LocalDateNumberConverter;
import excel.converters.localdatetime.LocalDateTimeDateConverter;
import excel.converters.localdatetime.LocalDateTimeStringConverter;
import excel.converters.longconverter.LongBooleanConverter;
import excel.converters.longconverter.LongNumberConverter;
import excel.converters.longconverter.LongStringConverter;
import excel.converters.shortconverter.ShortBooleanConverter;
import excel.converters.shortconverter.ShortNumberConverter;
import excel.converters.shortconverter.ShortStringConverter;
import excel.converters.string.StringBooleanConverter;
import excel.converters.string.StringErrorConverter;
import excel.converters.string.StringNumberConverter;
import excel.converters.string.StringStringConverter;
import excel.converters.url.UrlImageConverter;
import excel.util.MapUtils;

import java.util.Map;

/**
 * Load default handler
 *
 * @author Jiaju Zhuang
 */
public class DefaultConverterLoader {
    private static Map<ConverterKey, Converter<?>> defaultWriteConverter;
    private static Map<ConverterKey, Converter<?>> allConverter;

    static {
        initDefaultWriteConverter();
        initAllConverter();
    }

    private static void initAllConverter() {
        allConverter = MapUtils.newHashMapWithExpectedSize(40);
        putAllConverter(new BigDecimalBooleanConverter());
        putAllConverter(new BigDecimalNumberConverter());
        putAllConverter(new BigDecimalStringConverter());

        putAllConverter(new BigIntegerBooleanConverter());
        putAllConverter(new BigIntegerNumberConverter());
        putAllConverter(new BigIntegerStringConverter());

        putAllConverter(new BooleanBooleanConverter());
        putAllConverter(new BooleanNumberConverter());
        putAllConverter(new BooleanStringConverter());

        putAllConverter(new ByteBooleanConverter());
        putAllConverter(new ByteNumberConverter());
        putAllConverter(new ByteStringConverter());

        putAllConverter(new DateNumberConverter());
        putAllConverter(new DateStringConverter());

        putAllConverter(new LocalDateNumberConverter());
        putAllConverter(new LocalDateTimeStringConverter());

        putAllConverter(new DoubleBooleanConverter());
        putAllConverter(new DoubleNumberConverter());
        putAllConverter(new DoubleStringConverter());

        putAllConverter(new FloatBooleanConverter());
        putAllConverter(new FloatNumberConverter());
        putAllConverter(new FloatStringConverter());

        putAllConverter(new IntegerBooleanConverter());
        putAllConverter(new IntegerNumberConverter());
        putAllConverter(new IntegerStringConverter());

        putAllConverter(new LongBooleanConverter());
        putAllConverter(new LongNumberConverter());
        putAllConverter(new LongStringConverter());

        putAllConverter(new ShortBooleanConverter());
        putAllConverter(new ShortNumberConverter());
        putAllConverter(new ShortStringConverter());

        putAllConverter(new StringBooleanConverter());
        putAllConverter(new StringNumberConverter());
        putAllConverter(new StringStringConverter());
        putAllConverter(new StringErrorConverter());

        putAllConverter(new BigIntegerStringConverter());
    }

    private static void initDefaultWriteConverter() {
        defaultWriteConverter = MapUtils.newHashMapWithExpectedSize(40);
        putWriteConverter(new BigDecimalNumberConverter());
        putWriteConverter(new BigIntegerNumberConverter());
        putWriteConverter(new BooleanBooleanConverter());
        putWriteConverter(new ByteNumberConverter());
        putWriteConverter(new DateDateConverter());
        putWriteConverter(new LocalDateTimeDateConverter());
        putWriteConverter(new DoubleNumberConverter());
        putWriteConverter(new FloatNumberConverter());
        putWriteConverter(new IntegerNumberConverter());
        putWriteConverter(new LongNumberConverter());
        putWriteConverter(new ShortNumberConverter());
        putWriteConverter(new StringStringConverter());
        putWriteConverter(new FileImageConverter());
        putWriteConverter(new InputStreamImageConverter());
        putWriteConverter(new ByteArrayImageConverter());
        putWriteConverter(new BoxingByteArrayImageConverter());
        putWriteConverter(new UrlImageConverter());

        // In some cases, it must be converted to string
        putWriteStringConverter(new BigDecimalStringConverter());
        putWriteStringConverter(new BigIntegerStringConverter());
        putWriteStringConverter(new BooleanStringConverter());
        putWriteStringConverter(new ByteStringConverter());
        putWriteStringConverter(new DateStringConverter());
        putWriteStringConverter(new LocalDateTimeStringConverter());
        putWriteStringConverter(new DoubleStringConverter());
        putWriteStringConverter(new FloatStringConverter());
        putWriteStringConverter(new IntegerStringConverter());
        putWriteStringConverter(new LongStringConverter());
        putWriteStringConverter(new ShortStringConverter());
        putWriteStringConverter(new StringStringConverter());
        putWriteStringConverter(new BigIntegerStringConverter());
    }

    /**
     * Load default write converter
     *
     * @return
     */
    public static Map<ConverterKey, Converter<?>> loadDefaultWriteConverter() {
        return defaultWriteConverter;
    }

    private static void putWriteConverter(Converter<?> converter) {
        defaultWriteConverter.put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
    }

    private static void putWriteStringConverter(Converter<?> converter) {
        defaultWriteConverter.put(
            ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);
    }

    /**
     * Load default read converter
     *
     * @return
     */
    public static Map<ConverterKey, Converter<?>> loadDefaultReadConverter() {
        return loadAllConverter();
    }

    /**
     * Load all converter
     *
     * @return
     */
    public static Map<ConverterKey, Converter<?>> loadAllConverter() {
        return allConverter;
    }

    private static void putAllConverter(Converter<?> converter) {
        allConverter.put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()),
            converter);
    }
}
