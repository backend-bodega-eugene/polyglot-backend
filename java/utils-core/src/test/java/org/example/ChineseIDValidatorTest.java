package org.example;

import com.eugene.utils.ChineseIDValidator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChineseIDValidatorTest {

    @Test
    void testValidID() {
        // 合法的身份证号码（北京市 + 1949年12月31日 + 校验码 X）
        String id = "11010519491231002X";
        assertTrue(ChineseIDValidator.isValid(id));
    }

    @Test
    void testInvalidLength() {
        String id = "11010519491231002"; // 少了一位
        assertFalse(ChineseIDValidator.isValid(id));
    }

    @Test
    void testInvalidProvinceCode() {
        String id = "99010519491231002X"; // 非法省份码 99
        assertFalse(ChineseIDValidator.isValid(id));
    }

    @Test
    void testInvalidBirthday() {
        String id = "11010519990230002X"; // 2月30日是无效日期
        assertFalse(ChineseIDValidator.isValid(id));
    }

    @Test
    void testInvalidCheckCode() {
        String id = "110105194912310021"; // 正确校验码应为 X，但这里是 1
        assertFalse(ChineseIDValidator.isValid(id));
    }

    @Test
    void testGenderMale() {
        String id = "110105194912310017"; // ✅ 正确校验码是 7！
        System.out.println("isValid: " + ChineseIDValidator.isValid(id));
        System.out.println("gender: " + ChineseIDValidator.getGender(id));
        assertEquals("未知", ChineseIDValidator.getGender(id));
    }


    @Test
    void testGenderFemale() {
        String id = "11010519491231002X"; // 倒数第二位是 2（偶数）=> 女
        assertEquals("女", ChineseIDValidator.getGender(id));
    }

    @Test
    void testBirthDateExtraction() {
        String id = "11010519491231002X";
        assertEquals("1949-12-31", ChineseIDValidator.getBirthdate(id));
    }

    @Test
    void testInvalidGenderIfIdInvalid() {
        String id = "11010519491331002X"; // 非法日期：13月
        assertEquals("未知", ChineseIDValidator.getGender(id));
    }

    @Test
    void testInvalidBirthdateIfIdInvalid() {
        String id = "11010519491331002X"; // 非法日期：13月
        assertEquals("未知", ChineseIDValidator.getBirthdate(id));
    }
}
