package com.desco.userservice.enums;

/** Mirrors the native PostgreSQL enum type {@code area_name}. */
public enum AreaName {
    // The 8 original DESCO zones (Dhaka neighbourhoods). Kept because live
    // rows reference them and Postgres enums have no DROP VALUE.
    UTTARA, GULSHAN, BANANI, DHANMONDI,
    BASHUNDHARA, MIRPUR, BANASREE, BARIDHARA,

    // All 64 districts of Bangladesh, by division. Requires
    // db/02_area_nationwide.sql to have been applied — without it
    // Postgres rejects these labels and the insert fails as a 500.
    // Barishal
    BARGUNA, BARISHAL, BHOLA, JHALOKATI,
    PATUAKHALI, PIROJPUR,
    // Chattogram
    BANDARBAN, BRAHMANBARIA, CHANDPUR, CHATTOGRAM,
    CUMILLA, COXS_BAZAR, FENI, KHAGRACHHARI,
    LAKSHMIPUR, NOAKHALI, RANGAMATI,
    // Dhaka
    DHAKA, FARIDPUR, GAZIPUR, GOPALGANJ,
    KISHOREGANJ, MADARIPUR, MANIKGANJ, MUNSHIGANJ,
    NARAYANGANJ, NARSINGDI, RAJBARI, SHARIATPUR,
    TANGAIL,
    // Khulna
    BAGERHAT, CHUADANGA, JASHORE, JHENAIDAH,
    KHULNA, KUSHTIA, MAGURA, MEHERPUR,
    NARAIL, SATKHIRA,
    // Mymensingh
    JAMALPUR, MYMENSINGH, NETROKONA, SHERPUR,
    // Rajshahi
    BOGURA, CHAPAINAWABGANJ, JOYPURHAT, NAOGAON,
    NATORE, PABNA, RAJSHAHI, SIRAJGANJ,
    // Rangpur
    DINAJPUR, GAIBANDHA, KURIGRAM, LALMONIRHAT,
    NILPHAMARI, PANCHAGARH, RANGPUR, THAKURGAON,
    // Sylhet
    HABIGANJ, MOULVIBAZAR, SUNAMGANJ, SYLHET
}
