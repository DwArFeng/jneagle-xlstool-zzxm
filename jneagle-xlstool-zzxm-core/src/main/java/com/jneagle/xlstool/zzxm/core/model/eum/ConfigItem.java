package com.jneagle.xlstool.zzxm.core.model.eum;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dwarfeng.dutil.develop.setting.AbstractSettingInfo;
import com.dwarfeng.dutil.develop.setting.SettingEnumItem;
import com.dwarfeng.dutil.develop.setting.SettingInfo;
import com.dwarfeng.dutil.develop.setting.info.BooleanSettingInfo;
import com.dwarfeng.dutil.develop.setting.info.LocaleSettingInfo;
import com.dwarfeng.dutil.develop.setting.info.StringSettingInfo;

public enum ConfigItem implements SettingEnumItem {

	/** 国际化地区。 */
	I18N_LOCALE("i18n.locale", new LocaleSettingInfo("zh_CN")),

	/** Excel读取失败时最大的记录数。 */
	LOG_MAX_EXCEL_LOAD_WARNING("log.max-excel-load-warning", new PositiveIntegerSettingInfo("10")),
	/** Excel读取失败时最大的记录数。 */
	LOG_MAX_EXCEL_FILTER_WARNING("log.max-excel-filter-warning", new PositiveIntegerSettingInfo("10")),
	/** Excel导出失败时最大的记录数。 */
	LOG_MAX_EXCEL_EXPORT_WARNING("log.max-excel-export-warning", new PositiveIntegerSettingInfo("10")),

	/** 项目号过滤器，只有匹配该表达式的项目号才会被统计。 */
	POLICY_XMH_FILTER("policy.xmh-filter", new StringSettingInfo("^[^/]+$")),
	/** 是否启用项目号过滤器。 */
	POLICY_XMH_FILTER_ENABLED("policy.xmh-filter-enabled", new BooleanSettingInfo("true")),

	/** 源文件的总表所在的表单序号。 */
	SRCTABLE_INDEX_COUNT_SHEET("srctable.index.count-sheet", new PositiveIntegerSettingInfo("0")),
	/** 源文件的第一行数据行 */
	SRCTABLE_INDEX_ROW_FIRST_DATA("scrtable.index.row.first-data", new PositiveIntegerSettingInfo("2")),
	/** 导出文件的第一行表头行 */
	EXPTABLE_INDEX_ROW_FIRST_HEADER("exptable.index.row.first-header", new PositiveIntegerSettingInfo("0")),
	/** 导出文件的第一行数据行 */
	EXPTABLE_INDEX_ROW_FIRST_DATA("exptable.index.row.first-data", new PositiveIntegerSettingInfo("2")),

	/** 导出表的日期格式。 */
	EXPTABLE_DATE_FORMAT("exptable.date.format", new StringSettingInfo("yyyy-mm-dd")),
	
	/** 源文件的项目号所在的列。 */
	SRCTABLE_INDEX_COLUMN_XMH("srctable.index.column.xmh", new PositiveIntegerSettingInfo("4")),
	/** 源文件的部件号所在的列。 */
	SRCTABLE_INDEX_COLUMN_BJH("srctable.index.column.bjh", new PositiveIntegerSettingInfo("5")),
	/** 源文件的零件号所在的列。 */
	SRCTABLE_INDEX_COLUMN_LJH("srctable.index.column.ljh", new PositiveIntegerSettingInfo("6")),
	/** 源文件的零件名称所在的列。 */
	SRCTABLE_INDEX_COLUMN_LJMC("srctable.index.column.ljmc", new PositiveIntegerSettingInfo("7")),
	/** 源文件的材质所在的列。 */
	SRCTABLE_INDEX_COLUMN_CZ("srctable.index.column.cz", new PositiveIntegerSettingInfo("8")),
	/** 源文件的单需所在的列。 */
	SRCTABLE_INDEX_COLUMN_DX("srctable.index.column.dx", new PositiveIntegerSettingInfo("9")),
	/** 源文件的单重所在的列。 */
	SRCTABLE_INDEX_COLUMN_DZ("srctable.index.column.dz", new PositiveIntegerSettingInfo("10")),
	/** 源文件的总重所在的列。 */
	SRCTABLE_INDEX_COLUMN_ZZ("srctable.index.column.zz", new PositiveIntegerSettingInfo("11")),
	/** 源文件的台数所在的列。 */
	SRCTABLE_INDEX_COLUMN_TS("srctable.index.column.ts", new PositiveIntegerSettingInfo("12")),
	/** 源文件的总数所在的列。 */
	SRCTABLE_INDEX_COLUMN_ZS("srctable.index.column.zs", new PositiveIntegerSettingInfo("13")),
	/** 源文件的下图日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_XTRQ("srctable.index.column.xtrq", new PositiveIntegerSettingInfo("14")),
	/** 源文件的配套日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_PTRQ("srctable.index.column.ptrq", new PositiveIntegerSettingInfo("15")),
	/** 源文件的配套日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_BL_LB("srctable.index.column.bl-lb", new PositiveIntegerSettingInfo("16")),
	/** 源文件的四次工序委托方所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_WTF("srctable.index.column.scgx-wtf", new PositiveIntegerSettingInfo("17")),
	/** 源文件的四次工序数量所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_SL("srctable.index.column.scgx-sl", new PositiveIntegerSettingInfo("18")),
	/** 源文件的四次工序加工内容所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_JGNR("srctable.index.column.scgx-jgnr", new PositiveIntegerSettingInfo("19")),
	/** 源文件的四次工序单价所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_DJ("srctable.index.column.scgx-dj", new PositiveIntegerSettingInfo("20")),
	/** 源文件的四次工序开单日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_KDRQ("srctable.index.column.scgx-kdrq", new PositiveIntegerSettingInfo("21")),
	/** 源文件的四次工序入库日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_RKRQ("srctable.index.column.scgx-rkrq", new PositiveIntegerSettingInfo("22")),
	/** 源文件的四次工序入库数所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_RKS("srctable.index.column.scgx-rks", new PositiveIntegerSettingInfo("23")),
	/** 源文件的四次工序缺件所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_QJ("srctable.index.column.scgx-qj", new PositiveIntegerSettingInfo("24")),
	/** 源文件的四次工序序号所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_XH("srctable.index.column.scgx-xh", new PositiveIntegerSettingInfo("25")),
	/** 源文件的四次工序备注所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_BZ("srctable.index.column.scgx-bz", new PositiveIntegerSettingInfo("26")),
	/** 源文件的四次工序检验员所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_JYY("srctable.index.column.scgx-jyy", new PositiveIntegerSettingInfo("27")),
	/** 源文件的四次工序操作者所在的列。 */
	SRCTABLE_INDEX_COLUMN_SCGX_CZZ("srctable.index.column.scgx-czz", new PositiveIntegerSettingInfo("28")),
	/** 源文件的七次工序委托方所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_WTF("srctable.index.column.qcgx-wtf", new PositiveIntegerSettingInfo("29")),
	/** 源文件的七次工序数量所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_SL("srctable.index.column.qcgx-sl", new PositiveIntegerSettingInfo("30")),
	/** 源文件的七次工序加工内容所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_JGNR("srctable.index.column.qcgx-jgnr", new PositiveIntegerSettingInfo("31")),
	/** 源文件的七次工序单价所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_DJ("srctable.index.column.qcgx-dj", new PositiveIntegerSettingInfo("32")),
	/** 源文件的七次工序开单日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_KDRQ("srctable.index.column.qcgx-kdrq", new PositiveIntegerSettingInfo("33")),
	/** 源文件的七次工序入库日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_RKRQ("srctable.index.column.qcgx-rkrq", new PositiveIntegerSettingInfo("34")),
	/** 源文件的七次工序入库数所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_RKS("srctable.index.column.qcgx-rks", new PositiveIntegerSettingInfo("35")),
	/** 源文件的七次工序缺件所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_QJ("srctable.index.column.qcgx-qj", new PositiveIntegerSettingInfo("36")),
	/** 源文件的七次工序序号所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_XH("srctable.index.column.qcgx-xh", new PositiveIntegerSettingInfo("37")),
	/** 源文件的七次工序备注所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_BZ("srctable.index.column.qcgx-bz", new PositiveIntegerSettingInfo("38")),
	/** 源文件的七次工序检验员所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_JYY("srctable.index.column.qcgx-jyy", new PositiveIntegerSettingInfo("39")),
	/** 源文件的七次工序操作者所在的列。 */
	SRCTABLE_INDEX_COLUMN_QCGX_CZZ("srctable.index.column.qcgx-czz", new PositiveIntegerSettingInfo("40")),
	/** 源文件的八次工序委托方所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_WTF("srctable.index.column.bcgx-wtf", new PositiveIntegerSettingInfo("41")),
	/** 源文件的八次工序数量所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_SL("srctable.index.column.bcgx-sl", new PositiveIntegerSettingInfo("42")),
	/** 源文件的八次工序加工内容所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_JGNR("srctable.index.column.bcgx-jgnr", new PositiveIntegerSettingInfo("43")),
	/** 源文件的八次工序单价所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_DJ("srctable.index.column.bcgx-dj", new PositiveIntegerSettingInfo("44")),
	/** 源文件的八次工序开单日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_KDRQ("srctable.index.column.bcgx-kdrq", new PositiveIntegerSettingInfo("45")),
	/** 源文件的八次工序入库日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_RKRQ("srctable.index.column.bcgx-rkrq", new PositiveIntegerSettingInfo("46")),
	/** 源文件的八次工序入库数所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_RKS("srctable.index.column.bcgx-rks", new PositiveIntegerSettingInfo("47")),
	/** 源文件的八次工序缺件所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_QJ("srctable.index.column.bcgx-qj", new PositiveIntegerSettingInfo("48")),
	/** 源文件的八次工序序号所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_XH("srctable.index.column.bcgx-xh", new PositiveIntegerSettingInfo("49")),
	/** 源文件的八次工序备注所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_BZ("srctable.index.column.bcgx-bz", new PositiveIntegerSettingInfo("50")),
	/** 源文件的八次工序检验员所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_JYY("srctable.index.column.bcgx-jyy", new PositiveIntegerSettingInfo("51")),
	/** 源文件的八次工序操作者所在的列。 */
	SRCTABLE_INDEX_COLUMN_BCGX_CZZ("srctable.index.column.bcgx-czz", new PositiveIntegerSettingInfo("52")),
	/** 源文件的九次工序委托方所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_WTF("srctable.index.column.jcgx-wtf", new PositiveIntegerSettingInfo("53")),
	/** 源文件的九次工序数量所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_SL("srctable.index.column.jcgx-sl", new PositiveIntegerSettingInfo("54")),
	/** 源文件的九次工序加工内容所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_JGNR("srctable.index.column.jcgx-jgnr", new PositiveIntegerSettingInfo("55")),
	/** 源文件的九次工序单价所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_DJ("srctable.index.column.jcgx-dj", new PositiveIntegerSettingInfo("56")),
	/** 源文件的九次工序开单日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_KDRQ("srctable.index.column.jcgx-kdrq", new PositiveIntegerSettingInfo("57")),
	/** 源文件的九次工序入库日期所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_RKRQ("srctable.index.column.jcgx-rkrq", new PositiveIntegerSettingInfo("58")),
	/** 源文件的九次工序入库数所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_RKS("srctable.index.column.jcgx-rks", new PositiveIntegerSettingInfo("59")),
	/** 源文件的九次工序缺件所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_QJ("srctable.index.column.jcgx-qj", new PositiveIntegerSettingInfo("60")),
	/** 源文件的九次工序序号所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_XH("srctable.index.column.jcgx-xh", new PositiveIntegerSettingInfo("61")),
	/** 源文件的九次工序备注所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_BZ("srctable.index.column.jcgx-bz", new PositiveIntegerSettingInfo("62")),
	/** 源文件的九次工序检验员所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_JYY("srctable.index.column.jcgx-jyy", new PositiveIntegerSettingInfo("63")),
	/** 源文件的九次工序操作者所在的列。 */
	SRCTABLE_INDEX_COLUMN_JCGX_CZZ("srctable.index.column.jcgx-czz", new PositiveIntegerSettingInfo("64")),

	/** 导出文件的项目号所在的列。 */
	EXPTABLE_INDEX_COLUMN_XMH("exptable.index.column.xmh", new PositiveIntegerSettingInfo("0")),
	/** 导出文件的部件号所在的列。 */
	EXPTABLE_INDEX_COLUMN_BJH("exptable.index.column.bjh", new PositiveIntegerSettingInfo("1")),
	/** 导出文件的零件号所在的列。 */
	EXPTABLE_INDEX_COLUMN_LJH("exptable.index.column.ljh", new PositiveIntegerSettingInfo("2")),
	/** 导出文件的零件名称所在的列。 */
	EXPTABLE_INDEX_COLUMN_LJMC("exptable.index.column.ljmc", new PositiveIntegerSettingInfo("3")),
	/** 导出文件的材质所在的列。 */
	EXPTABLE_INDEX_COLUMN_CZ("exptable.index.column.cz", new PositiveIntegerSettingInfo("4")),
	/** 导出文件的单需所在的列。 */
	EXPTABLE_INDEX_COLUMN_DX("exptable.index.column.dx", new PositiveIntegerSettingInfo("5")),
	/** 导出文件的单重所在的列。 */
	EXPTABLE_INDEX_COLUMN_DZ("exptable.index.column.dz", new PositiveIntegerSettingInfo("6")),
	/** 导出文件的总重所在的列。 */
	EXPTABLE_INDEX_COLUMN_ZZ("exptable.index.column.zz", new PositiveIntegerSettingInfo("7")),
	/** 导出文件的台数所在的列。 */
	EXPTABLE_INDEX_COLUMN_TS("exptable.index.column.ts", new PositiveIntegerSettingInfo("8")),
	/** 导出文件的总数所在的列。 */
	EXPTABLE_INDEX_COLUMN_ZS("exptable.index.column.zs", new PositiveIntegerSettingInfo("9")),
	/** 导出文件的下图日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_XTRQ("exptable.index.column.xtrq", new PositiveIntegerSettingInfo("10")),
	/** 导出文件的配套日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_PTRQ("exptable.index.column.ptrq", new PositiveIntegerSettingInfo("11")),
	/** 导出文件的配套日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_BL_LB("exptable.index.column.bl-lb", new PositiveIntegerSettingInfo("12")),
	/** 导出文件的四次工序委托方所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_WTF("exptable.index.column.scgx-wtf", new PositiveIntegerSettingInfo("13")),
	/** 导出文件的四次工序数量所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_SL("exptable.index.column.scgx-sl", new PositiveIntegerSettingInfo("14")),
	/** 导出文件的四次工序加工内容所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_JGNR("exptable.index.column.scgx-jgnr", new PositiveIntegerSettingInfo("15")),
	/** 导出文件的四次工序单价所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_DJ("exptable.index.column.scgx-dj", new PositiveIntegerSettingInfo("16")),
	/** 导出文件的四次工序开单日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_KDRQ("exptable.index.column.scgx-kdrq", new PositiveIntegerSettingInfo("17")),
	/** 导出文件的四次工序入库日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_RKRQ("exptable.index.column.scgx-rkrq", new PositiveIntegerSettingInfo("18")),
	/** 导出文件的四次工序入库数所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_RKS("exptable.index.column.scgx-rks", new PositiveIntegerSettingInfo("19")),
	/** 导出文件的四次工序缺件所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_QJ("exptable.index.column.scgx-qj", new PositiveIntegerSettingInfo("20")),
	/** 导出文件的四次工序序号所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_XH("exptable.index.column.scgx-xh", new PositiveIntegerSettingInfo("21")),
	/** 导出文件的四次工序备注所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_BZ("exptable.index.column.scgx-bz", new PositiveIntegerSettingInfo("22")),
	/** 导出文件的四次工序检验员所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_JYY("exptable.index.column.scgx-jyy", new PositiveIntegerSettingInfo("23")),
	/** 导出文件的四次工序操作者所在的列。 */
	EXPTABLE_INDEX_COLUMN_SCGX_CZZ("exptable.index.column.scgx-czz", new PositiveIntegerSettingInfo("24")),
	/** 导出文件的七次工序委托方所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_WTF("exptable.index.column.qcgx-wtf", new PositiveIntegerSettingInfo("25")),
	/** 导出文件的七次工序数量所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_SL("exptable.index.column.qcgx-sl", new PositiveIntegerSettingInfo("26")),
	/** 导出文件的七次工序加工内容所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_JGNR("exptable.index.column.qcgx-jgnr", new PositiveIntegerSettingInfo("27")),
	/** 导出文件的七次工序单价所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_DJ("exptable.index.column.qcgx-dj", new PositiveIntegerSettingInfo("28")),
	/** 导出文件的七次工序开单日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_KDRQ("exptable.index.column.qcgx-kdrq", new PositiveIntegerSettingInfo("29")),
	/** 导出文件的七次工序入库日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_RKRQ("exptable.index.column.qcgx-rkrq", new PositiveIntegerSettingInfo("30")),
	/** 导出文件的七次工序入库数所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_RKS("exptable.index.column.qcgx-rks", new PositiveIntegerSettingInfo("31")),
	/** 导出文件的七次工序缺件所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_QJ("exptable.index.column.qcgx-qj", new PositiveIntegerSettingInfo("32")),
	/** 导出文件的七次工序序号所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_XH("exptable.index.column.qcgx-xh", new PositiveIntegerSettingInfo("33")),
	/** 导出文件的七次工序备注所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_BZ("exptable.index.column.qcgx-bz", new PositiveIntegerSettingInfo("34")),
	/** 导出文件的七次工序检验员所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_JYY("exptable.index.column.qcgx-jyy", new PositiveIntegerSettingInfo("35")),
	/** 导出文件的七次工序操作者所在的列。 */
	EXPTABLE_INDEX_COLUMN_QCGX_CZZ("exptable.index.column.qcgx-czz", new PositiveIntegerSettingInfo("36")),
	/** 导出文件的八次工序委托方所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_WTF("exptable.index.column.bcgx-wtf", new PositiveIntegerSettingInfo("37")),
	/** 导出文件的八次工序数量所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_SL("exptable.index.column.bcgx-sl", new PositiveIntegerSettingInfo("38")),
	/** 导出文件的八次工序加工内容所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_JGNR("exptable.index.column.bcgx-jgnr", new PositiveIntegerSettingInfo("39")),
	/** 导出文件的八次工序单价所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_DJ("exptable.index.column.bcgx-dj", new PositiveIntegerSettingInfo("40")),
	/** 导出文件的八次工序开单日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_KDRQ("exptable.index.column.bcgx-kdrq", new PositiveIntegerSettingInfo("41")),
	/** 导出文件的八次工序入库日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_RKRQ("exptable.index.column.bcgx-rkrq", new PositiveIntegerSettingInfo("42")),
	/** 导出文件的八次工序入库数所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_RKS("exptable.index.column.bcgx-rks", new PositiveIntegerSettingInfo("43")),
	/** 导出文件的八次工序缺件所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_QJ("exptable.index.column.bcgx-qj", new PositiveIntegerSettingInfo("44")),
	/** 导出文件的八次工序序号所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_XH("exptable.index.column.bcgx-xh", new PositiveIntegerSettingInfo("45")),
	/** 导出文件的八次工序备注所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_BZ("exptable.index.column.bcgx-bz", new PositiveIntegerSettingInfo("46")),
	/** 导出文件的八次工序检验员所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_JYY("exptable.index.column.bcgx-jyy", new PositiveIntegerSettingInfo("47")),
	/** 导出文件的八次工序操作者所在的列。 */
	EXPTABLE_INDEX_COLUMN_BCGX_CZZ("exptable.index.column.bcgx-czz", new PositiveIntegerSettingInfo("48")),
	/** 导出文件的九次工序委托方所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_WTF("exptable.index.column.jcgx-wtf", new PositiveIntegerSettingInfo("49")),
	/** 导出文件的九次工序数量所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_SL("exptable.index.column.jcgx-sl", new PositiveIntegerSettingInfo("50")),
	/** 导出文件的九次工序加工内容所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_JGNR("exptable.index.column.jcgx-jgnr", new PositiveIntegerSettingInfo("51")),
	/** 导出文件的九次工序单价所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_DJ("exptable.index.column.jcgx-dj", new PositiveIntegerSettingInfo("52")),
	/** 导出文件的九次工序开单日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_KDRQ("exptable.index.column.jcgx-kdrq", new PositiveIntegerSettingInfo("53")),
	/** 导出文件的九次工序入库日期所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_RKRQ("exptable.index.column.jcgx-rkrq", new PositiveIntegerSettingInfo("54")),
	/** 导出文件的九次工序入库数所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_RKS("exptable.index.column.jcgx-rks", new PositiveIntegerSettingInfo("55")),
	/** 导出文件的九次工序缺件所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_QJ("exptable.index.column.jcgx-qj", new PositiveIntegerSettingInfo("56")),
	/** 导出文件的九次工序序号所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_XH("exptable.index.column.jcgx-xh", new PositiveIntegerSettingInfo("57")),
	/** 导出文件的九次工序备注所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_BZ("exptable.index.column.jcgx-bz", new PositiveIntegerSettingInfo("58")),
	/** 导出文件的九次工序检验员所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_JYY("exptable.index.column.jcgx-jyy", new PositiveIntegerSettingInfo("59")),
	/** 导出文件的九次工序操作者所在的列。 */
	EXPTABLE_INDEX_COLUMN_JCGX_CZZ("exptable.index.column.jcgx-czz", new PositiveIntegerSettingInfo("60")),

	;
	private static final class PositiveIntegerSettingInfo extends AbstractSettingInfo implements SettingInfo {

		private static final int RADIX = 10;
		private String lastCheckedValue = null;
		private Integer lastParsedValue = null;
		private final Lock lock = new ReentrantLock();

		public PositiveIntegerSettingInfo(String defaultValue) throws NullPointerException, IllegalArgumentException {
			super(defaultValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return PositiveIntegerSettingInfo.class.hashCode() * 61 + defaultValue.hashCode() * 17;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (Objects.isNull(obj))
				return false;
			if (!(obj.getClass() == PositiveIntegerSettingInfo.class))
				return false;

			PositiveIntegerSettingInfo that = (PositiveIntegerSettingInfo) obj;
			return Objects.equals(this.defaultValue, that.defaultValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "PositiveIntegerSettingInfo [defaultValue=" + defaultValue + "]";
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean isNonNullValid(String value) {
			lock.lock();
			try {
				if (Objects.equals(value, lastCheckedValue))
					return Objects.nonNull(lastParsedValue);

				try {
					lastCheckedValue = value;
					lastParsedValue = Integer.parseInt(value, RADIX);
					if (lastParsedValue < 0) {
						lastParsedValue = null;
						return false;
					}
				} catch (Exception e) {
					lastParsedValue = null;
					return false;
				}
				return true;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object parseValidValue(String value) {
			lock.lock();
			try {
				if (Objects.equals(value, lastCheckedValue))
					return lastParsedValue;

				try {
					lastCheckedValue = value;
					lastParsedValue = Integer.parseInt(value, RADIX);
					return lastParsedValue;
				} catch (Exception e) {
					lastCheckedValue = null;
					lastParsedValue = null;
					throw new IllegalStateException();
				}
			} finally {
				lock.unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String parseNonNullObject(Object object) {
			if (!(object instanceof Integer))
				return null;

			if ((Integer) object < 0)
				return null;

			return Integer.toString((int) object, RADIX);
		}

	}

	private final String name;
	private final SettingInfo settingInfo;

	private ConfigItem(String name, SettingInfo settingInfo) {
		this.name = name;
		this.settingInfo = settingInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SettingInfo getSettingInfo() {
		return settingInfo;
	}

}
