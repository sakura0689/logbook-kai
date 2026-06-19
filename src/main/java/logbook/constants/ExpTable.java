package logbook.constants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 経験値テーブル
 *
 */
public class ExpTable {

    /** 艦娘MaxLv */
    public static final int MAX_LEVEL = 188;
    
    private static final int KEKKON_KARI_EXP = 1000000;
    
    /**
     * 経験値テーブルプリセット値
     */
    private static final Map<Integer, Integer> EXP_TABLE;

    static {
        EXP_TABLE = new LinkedHashMap<>();
        EXP_TABLE.put(1, 0);
        EXP_TABLE.put(2, 100);
        EXP_TABLE.put(3, 300);
        EXP_TABLE.put(4, 600);
        EXP_TABLE.put(5, 1000);
        EXP_TABLE.put(6, 1500);
        EXP_TABLE.put(7, 2100);
        EXP_TABLE.put(8, 2800);
        EXP_TABLE.put(9, 3600);
        EXP_TABLE.put(10, 4500);
        EXP_TABLE.put(11, 5500);
        EXP_TABLE.put(12, 6600);
        EXP_TABLE.put(13, 7800);
        EXP_TABLE.put(14, 9100);
        EXP_TABLE.put(15, 10500);
        EXP_TABLE.put(16, 12000);
        EXP_TABLE.put(17, 13600);
        EXP_TABLE.put(18, 15300);
        EXP_TABLE.put(19, 17100);
        EXP_TABLE.put(20, 19000);
        EXP_TABLE.put(21, 21000);
        EXP_TABLE.put(22, 23100);
        EXP_TABLE.put(23, 25300);
        EXP_TABLE.put(24, 27600);
        EXP_TABLE.put(25, 30000);
        EXP_TABLE.put(26, 32500);
        EXP_TABLE.put(27, 35100);
        EXP_TABLE.put(28, 37800);
        EXP_TABLE.put(29, 40600);
        EXP_TABLE.put(30, 43500);
        EXP_TABLE.put(31, 46500);
        EXP_TABLE.put(32, 49600);
        EXP_TABLE.put(33, 52800);
        EXP_TABLE.put(34, 56100);
        EXP_TABLE.put(35, 59500);
        EXP_TABLE.put(36, 63000);
        EXP_TABLE.put(37, 66600);
        EXP_TABLE.put(38, 70300);
        EXP_TABLE.put(39, 74100);
        EXP_TABLE.put(40, 78000);
        EXP_TABLE.put(41, 82000);
        EXP_TABLE.put(42, 86100);
        EXP_TABLE.put(43, 90300);
        EXP_TABLE.put(44, 94600);
        EXP_TABLE.put(45, 99000);
        EXP_TABLE.put(46, 103500);
        EXP_TABLE.put(47, 108100);
        EXP_TABLE.put(48, 112800);
        EXP_TABLE.put(49, 117600);
        EXP_TABLE.put(50, 122500);
        EXP_TABLE.put(51, 127500);
        EXP_TABLE.put(52, 132700);
        EXP_TABLE.put(53, 138100);
        EXP_TABLE.put(54, 143700);
        EXP_TABLE.put(55, 149500);
        EXP_TABLE.put(56, 155500);
        EXP_TABLE.put(57, 161700);
        EXP_TABLE.put(58, 168100);
        EXP_TABLE.put(59, 174700);
        EXP_TABLE.put(60, 181500);
        EXP_TABLE.put(61, 188500);
        EXP_TABLE.put(62, 195800);
        EXP_TABLE.put(63, 203400);
        EXP_TABLE.put(64, 211300);
        EXP_TABLE.put(65, 219500);
        EXP_TABLE.put(66, 228000);
        EXP_TABLE.put(67, 236800);
        EXP_TABLE.put(68, 245900);
        EXP_TABLE.put(69, 255300);
        EXP_TABLE.put(70, 265000);
        EXP_TABLE.put(71, 275000);
        EXP_TABLE.put(72, 285400);
        EXP_TABLE.put(73, 296200);
        EXP_TABLE.put(74, 307400);
        EXP_TABLE.put(75, 319000);
        EXP_TABLE.put(76, 331000);
        EXP_TABLE.put(77, 343400);
        EXP_TABLE.put(78, 356200);
        EXP_TABLE.put(79, 369400);
        EXP_TABLE.put(80, 383000);
        EXP_TABLE.put(81, 397000);
        EXP_TABLE.put(82, 411500);
        EXP_TABLE.put(83, 426500);
        EXP_TABLE.put(84, 442000);
        EXP_TABLE.put(85, 458000);
        EXP_TABLE.put(86, 474500);
        EXP_TABLE.put(87, 491500);
        EXP_TABLE.put(88, 509000);
        EXP_TABLE.put(89, 527000);
        EXP_TABLE.put(90, 545500);
        EXP_TABLE.put(91, 564500);
        EXP_TABLE.put(92, 584500);
        EXP_TABLE.put(93, 606500);
        EXP_TABLE.put(94, 631500);
        EXP_TABLE.put(95, 661500);
        EXP_TABLE.put(96, 701500);
        EXP_TABLE.put(97, 761500);
        EXP_TABLE.put(98, 851500);
        EXP_TABLE.put(99, 1000000);
        EXP_TABLE.put(100, KEKKON_KARI_EXP);
        EXP_TABLE.put(101, 10000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(102, 11000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(103, 13000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(104, 16000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(105, 20000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(106, 25000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(107, 31000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(108, 38000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(109, 46000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(110, 55000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(111, 65000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(112, 77000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(113, 91000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(114, 107000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(115, 125000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(116, 145000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(117, 168000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(118, 194000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(119, 223000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(120, 255000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(121, 290000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(122, 329000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(123, 372000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(124, 419000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(125, 470000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(126, 525000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(127, 584000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(128, 647000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(129, 714000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(130, 785000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(131, 860000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(132, 940000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(133, 1025000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(134, 1115000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(135, 1210000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(136, 1310000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(137, 1415000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(138, 1525000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(139, 1640000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(140, 1760000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(141, 1887000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(142, 2021000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(143, 2162000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(144, 2310000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(145, 2465000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(146, 2628000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(147, 2799000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(148, 2978000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(149, 3165000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(150, 3360000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(151, 3564000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(152, 3777000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(153, 3999000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(154, 4230000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(155, 4470000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(156, 4720000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(157, 4780000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(158, 4860000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(159, 4970000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(160, 5120000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(161, 5320000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(162, 5580000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(163, 5910000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(164, 6320000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(165, 6820000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(166, 6920000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(167, 7033000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(168, 7172000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(169, 7350000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(170, 7580000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(171, 7875000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(172, 8248000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(173, 8705000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(174, 9266000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(175, 9950000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(176, 10100000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(177, 10300000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(178, 10600000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(179, 11100000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(180, 12000000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(181, 12200000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(182, 12600000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(183, 13200000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(184, 14000000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(185, 15000000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(186, 16200000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(187, 17600000 + KEKKON_KARI_EXP);
        EXP_TABLE.put(188, 19200000 + KEKKON_KARI_EXP);
    }

    /**
     * 経験値テーブルを取得します
     *
     * @return 経験値テーブル
     */
    public static Map<Integer, Integer> get() {
        return EXP_TABLE;
    }

    /**
     * 最大Lvを取得します
     *
     * @return 最大Lv
     * @deprecated
     */
    public static int maxLv() {
        return MAX_LEVEL;
    }
}
