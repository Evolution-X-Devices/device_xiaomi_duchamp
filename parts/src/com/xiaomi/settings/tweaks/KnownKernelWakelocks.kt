/*
 * ==============================================================
 *    __  __  _____   __    ___    ___   _____   _
 *   |  \/  ||_   _| / /_  ( _ )  / _ \ |___  | / \
 *   | |\/| |  | |  | '_ \ / _ \ | (_) |   / / / _ \
 *   | |  | |  | |  | (_) | (_) | \__, |  / / / ___ \
 *   |_|  |_|  |_|   \___/ \___/    /_/  /_/ /_/   \_\
 *
 *  SPDX-FileCopyRightText: The LineageOS Project
 *  SPDX-FileCopyRightText: 2026 MT6897A
 *  SPDX-License-Identifier: Apache-2.0
 * ==============================================================
 */

package com.xiaomi.settings.tweaks

import com.xiaomi.settings.R

object KnownKernelWakelocks {
    val ITEMS: Map<String, Pair<WakelockDangerLevel, Int>> = mapOf(
        // safe
        "batterysecret" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_batterysecret_desc
        ),
        "bms" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_bms_desc
        ),
        "bms suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_bms_desc
        ),
        "mtk-gauge" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_mtk_gauge_desc
        ),
        "gauge" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_mtk_gauge_desc
        ),
        "3-0028" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_3_0028_desc
        ),
        "gauge coulomb wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_gauge_coulomb_desc
        ),
        "coulomb" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_gauge_coulomb_desc
        ),
        "primary_chg" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_primary_chg_desc
        ),
        "cp_master" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_cp_master_desc
        ),
        "mtk-mst-hvdiv-chg" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_mtk_mst_hvdiv_desc
        ),
        "mtk-slv-hvdiv-chg" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_mtk_slv_hvdiv_desc
        ),
        "mtk-mst-div-chg" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_mtk_mst_hvdiv_desc
        ),
        "mtk-slv-div-chg" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_mtk_slv_hvdiv_desc
        ),
        "PE+ suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe_plus_desc
        ),
        "PE+" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe_plus_desc
        ),
        "PE4.0 suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe40_desc
        ),
        "PE4.0" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe40_desc
        ),
        "PE4.5 suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe45_desc
        ),
        "PE4.5" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe45_desc
        ),
        "PE+20 suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe20_desc
        ),
        "PE+20" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe20_desc
        ),
        "bp_notify_lock wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_bp_notify_desc
        ),
        "bp_notify_lock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_bp_notify_desc
        ),
        "vbat0_ws" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_vbat0_desc
        ),
        "charger suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_charger_desc
        ),
        "charger" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_charger_desc
        ),
        "mtk-master-charger" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_charger_desc
        ),
        "battery" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_battery_desc
        ),
        "btmtk_fwdump_wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_bt_fwdump_desc
        ),

        // caution
        "10001000.consys" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_10001000_consys_desc
        ),
        "bt_drv_io" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_bt_drv_io_desc
        ),
        "bt_psm" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_bt_psm_desc
        ),
        "btevent_eint" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_bt_woble_desc
        ),
        "btmtk_woble_wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_bt_woble_desc
        ),
        "wlan" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_wlan_desc
        ),
        "conn_power" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_conn_power_desc
        ),
        "conninfraFuncCtrl" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_conn_power_desc
        ),
        "gpsdl_wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_gpsdl_desc
        ),
        "tcpc_attach_wake_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_tcpc_attach_desc
        ),
        "tcpc_detach_wake_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_tcpc_detach_desc
        ),
        "type_c_port0" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_type_c_port0_desc
        ),
        "tcpc" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_tcpc_desc
        ),
        "usb" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_usb_desc
        ),
        "usb_boost" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_usb_boost_desc
        ),
        "11201000.usb0" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_usb_desc
        ),
        "0-005a" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_0_005a_desc
        ),
        "0-04" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_i2c_chip_desc
        ),
        "0-05" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_i2c_chip_desc
        ),
        "0-06" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_i2c_chip_desc
        ),
        "0-07" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_i2c_chip_desc
        ),
        "0-08" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_i2c_chip_desc
        ),
        "5-0034" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_i2c_chip_desc
        ),
        "11f01000.i2c:mt6375@34:adc" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_mt6375_desc
        ),
        "11f01000.i2c:mt6375@34:auxadc" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_mt6375_desc
        ),
        "11f01000.i2c:mt6375@34:tcpc" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_mt6375_desc
        ),
        "accdet_wake_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_accdet_desc
        ),
        "accdet_timer_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_accdet_desc
        ),
        "elliptic_wake_source" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_elliptic_desc
        ),
        "1e000000.adspsys" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_adspsys_desc
        ),
        "adsp wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_adsp_wakelock_desc
        ),
        "adsp" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_adsp_wakelock_desc
        ),
        "adsp_audio_wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_adsp_wakelock_desc
        ),
        "Offload wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_audio_offload_desc
        ),
        "Offload" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_audio_offload_desc
        ),
        "vow wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_vow_desc
        ),
        "vow" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_vow_desc
        ),
        "vow dump wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_vow_desc
        ),
        "vow ipi wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_vow_desc
        ),
        "1f800000.mmlsys-config" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_mmlsys_desc
        ),
        "mml_pm_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_mml_pm_lock_desc
        ),
        "mdp_pm_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_mdp_pm_lock_desc
        ),
        "ufs_wake_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_ufs_desc
        ),
        "scp wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_scp_desc
        ),
        "scp" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_scp_desc
        ),
        "scp reset wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_scp_reset_desc
        ),
        "vcp reset wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_vcp_reset_desc
        ),
        "vcp" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_vcp_desc
        ),
        "lpm_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_lpm_desc
        ),
        "isp_lock_wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_isp_desc
        ),
        "dip_lock_wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_dip_desc
        ),
        "WPE_lock_wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_wpe_desc
        ),
        "mt6989_apusys_rv" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_apu_rv_desc
        ),
        "mt6897_apupwr" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_apu_rv_desc
        ),
        "sensor0" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_sensor_desc
        ),
        "sensor1" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_sensor_desc
        ),
        "sensor2" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_sensor_desc
        ),
        "sensor3" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_sensor_desc
        ),
        "slbc" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_slbc_desc
        ),
        "timesync" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_timesync_desc
        ),
        "dlpt_notify_ws wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_dlpt_desc
        ),
        "dlpt_notify_ws" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_dlpt_desc
        ),
        "dump" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_ipi_desc
        ),
        "ipi" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_ipi_desc
        ),
        "ultra wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_ultra_desc
        ),
        "ultra" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_ultra_desc
        ),

        // danger
        "ccmni_md1" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_md1_desc
        ),
        "ccmni0" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni1" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni2" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni3" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni4" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni5" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni6" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni7" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni9" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni10" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni11" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni12" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni13" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni14" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni15" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni16" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni17" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni18" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni19" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccmni20" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_desc
        ),
        "ccci_fs" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_fs_desc
        ),
        "ccci_md_log" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_md_log_desc
        ),
        "ccci_0_200" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_0_202" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_0_204" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ipc_1220_0" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ipc_2" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ipc_3" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ipc_4" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ipc_5" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ipc_6" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ipc_7" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ipc_8" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ipc_9" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ipc_desc
        ),
        "ccci_ioctl0" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ioctl_desc
        ),
        "ccci_ioctl1" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ioctl_desc
        ),
        "ccci_ioctl2" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ioctl_desc
        ),
        "ccci_ioctl3" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ioctl_desc
        ),
        "ccci_ioctl4" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ioctl_desc
        ),
        "ccci_aud" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_aud_desc
        ),
        "ccci_bip" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_bip_desc
        ),
        "ccci_c2k_agps" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_c2k_desc
        ),
        "ccci_c2k_ppp" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_c2k_desc
        ),
        "ccci_ccb_ctrl" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ccb_desc
        ),
        "ccci_ccb_dhl" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ccb_desc
        ),
        "ccci_ccb_md_monitor" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ccb_desc
        ),
        "ccci_ccb_meta" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ccb_desc
        ),
        "ccci_ctrl" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_epdg1" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_epdg_desc
        ),
        "ccci_epdg2" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_epdg_desc
        ),
        "ccci_epdg3" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_epdg_desc
        ),
        "ccci_epdg4" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_epdg_desc
        ),
        "ccci_ikeraw" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_epdg_desc
        ),
        "ccci_imsa" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ims_desc
        ),
        "ccci_imsc" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ims_desc
        ),
        "ccci_imsdc" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ims_desc
        ),
        "ccci_imsem" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ims_desc
        ),
        "ccci_imsm" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ims_desc
        ),
        "ccci_imsv" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_ims_desc
        ),
        "ccci_it" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_lb_it" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_md_log_ctrl" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_mdl_monitor" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_poll" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_raw_audio" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_raw_desc
        ),
        "ccci_raw_dbm" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_raw_desc
        ),
        "ccci_raw_dhl" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_raw_desc
        ),
        "ccci_raw_lwa" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_raw_desc
        ),
        "ccci_raw_mdm" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_raw_desc
        ),
        "ccci_raw_netd" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_raw_desc
        ),
        "ccci_raw_usb" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_raw_desc
        ),
        "ccci_rpc" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_rpc_desc
        ),
        "ccci_rpc_k" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_rpc_desc
        ),
        "ccci_ss_xcap" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_sys" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_udc" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_vts" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_wifi_proxy" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "ccci_woa" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_system_desc
        ),
        "md_wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_md_dma_desc
        ),
        "md_cldma_peer" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_md_dma_desc
        ),
        "md_cldma_trm" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_md_dma_desc
        ),
        "trans_data" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_md_dma_desc
        ),
        "ttyC0" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttyc_desc
        ),
        "ttyC1" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttyc_desc
        ),
        "ttyC2" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttyc_desc
        ),
        "ttyC3" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttyc_desc
        ),
        "ttyC5" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttyc_desc
        ),
        "ttyC6" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttyc_desc
        ),
        "ttyCMIPC0" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc_desc
        ),
        "ttyCMIPC1" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc1_desc
        ),
        "ttyCMIPC2" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc_desc
        ),
        "ttyCMIPC3" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc_desc
        ),
        "ttyCMIPC4" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc_desc
        ),
        "ttyCMIPC5" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc_desc
        ),
        "ttyCMIPC6" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc_desc
        ),
        "ttyCMIPC7" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc_desc
        ),
        "ttyCMIPC8" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc_desc
        ),
        "ttyCMIPC9" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc_desc
        ),
        "radio-interface" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_radio_interface_desc
        ),
        "pmif_m wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pmif_m_desc
        ),
        "pmif_m" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pmif_desc
        ),
        "pmif_p wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pmif_desc
        ),
        "pmif_p" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pmif_desc
        ),
        "goodix_ts.0" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_goodix_ts_desc
        ),
        "odm:xiaomi_fingerprint" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_xiaomi_fp_desc
        ),
        "fp_wakesrc" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_xiaomi_fp_desc
        ),
        "pwrkey wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pwrkey_desc
        ),
        "pwrkey" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pwrkey_desc
        ),
        "kpd wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_kpd_desc
        ),
        "kpd" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_kpd_desc
        ),
        "alarmtimer.0.auto" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_alarmtimer_desc
        ),
        "mt6685-rtc" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_mt6685_rtc_desc
        ),
        "mt6685-rtc suspend wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_mt6685_rtc_suspend_desc
        ),
        "event0" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_event_desc
        ),
        "event1" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_event_desc
        ),
        "event2" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_event_desc
        ),
        "event4" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_event_desc
        ),
        "event5" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_event_desc
        ),
        "event6" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_event_desc
        ),
        "eventpoll" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_kernel_core_desc
        ),
        "[timerfd]" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_kernel_core_desc
        ),
        "inotify" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_kernel_core_desc
        ),
        "NETLINK" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_kernel_core_desc
        ),
        "reset" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_reset_desc
        ),
        "suspend" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_suspend_core_desc
        ),
        "wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_generic_desc
        ),
        "SuspendControl.TotalSuspendDelay" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_suspend_control_total_desc
        ),
        "SuspendControl.NormalSuspendDelay" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_suspend_control_normal_desc
        ),
        "SuspendControl.FailedSuspendDelay" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_suspend_control_failed_desc
        ),
        "userspace-abort" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_userspace_abort_desc
        ),
        "PowerManagerService.WakeLocks" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pms_wakelocks_desc
        ),
        "PowerManagerService.Display" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pms_display_desc
        ),
        "PowerManagerService.Broadcasts" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pms_broadcasts_desc
        )
    )
}
