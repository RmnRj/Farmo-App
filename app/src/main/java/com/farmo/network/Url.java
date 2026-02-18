package com.farmo.network;

public class Url {
    // ================== Authentication ==================
    private static final String CHECK_USERID = "api/auth/check-userid";
    private static final String LOGIN = "api/auth/login";
    private static final String LOGIN_WITH_TOKEN = "api/auth/login-with-token";
    private static final String LOGIN_CHANGE_PASSWORD = "api/auth/login-change-password";
    private static final String LOGOUT = "api/auth/logout";
    private static final String LOGOUT_ALL = "api/auth/logout-all";
    private static final String REGISTER = "api/auth/register";
    private static final String FORGOT_PASSWORD = "api/auth/forgot-password";
    private static final String FORGOT_PASSWORD_VERIFY_EMAIL = "api/auth/forgot-password-verify-email";
    private static final String FORGOT_PASSWORD_VERIFY_OTP = "api/auth/forgot-password-verify-otp";
    private static final String FORGOT_PASSWORD_CHANGE_PASSWORD = "api/auth/forgot-password-change-password";

    public static String getCheckUserid() { return CHECK_USERID; }
    public static String getLogin() { return LOGIN; }
    public static String getLoginWithToken() { return LOGIN_WITH_TOKEN; }
    public static String getLoginChangePassword() { return LOGIN_CHANGE_PASSWORD; }
    public static String getLogout() { return LOGOUT; }
    public static String getLogoutAll() { return LOGOUT_ALL; }
    public static String getRegister() { return REGISTER; }
    public static String getForgotPassword() { return FORGOT_PASSWORD; }
    public static String getForgotPasswordVerifyEmail() { return FORGOT_PASSWORD_VERIFY_EMAIL; }
    public static String getForgotPasswordVerifyOtp() { return FORGOT_PASSWORD_VERIFY_OTP; }
    public static String getForgotPasswordChangePassword() { return FORGOT_PASSWORD_CHANGE_PASSWORD; }

    // ================== User ==================
    private static final String UPDATE_PROFILE_PICTURE = "api/user/update-profile-picture";
    private static final String VERIFICATION_REQUEST = "api/user/verification-request";
    private static final String ONLINE_STATUS = "api/user/online-status";
    private static final String PROFILE = "api/user/profile";
    private static final String SEARCH_USER = "api/user/search";
    private static final String USER_FARMER_PAGE = "api/user/farmer";
    private static final String USER_CONSUMER_PAGE = "api/user/consumer";
    private static final String ADDRESS = "api/user/address";
    private static final String ADD_PAYMENT_METHOD = "api/user/payment-method";
    private static final String GET_PAYMENT_METHOD = "api/user/get-payment-method";

    public static String getUpdateProfilePicture() { return UPDATE_PROFILE_PICTURE; }
    public static String getVerificationRequest() { return VERIFICATION_REQUEST; }
    public static String getOnlineStatus() { return ONLINE_STATUS; }
    public static String getProfile() { return PROFILE; }
    public static String getSearchUser() { return SEARCH_USER; }
    public static String getUserFarmerPage() { return USER_FARMER_PAGE; }
    public static String getUserConsumerPage() { return USER_CONSUMER_PAGE; }
    public static String getAddress() { return ADDRESS; }
    public static String getAddPaymentMethod() { return ADD_PAYMENT_METHOD; }
    public static String getGetPaymentMethod() { return GET_PAYMENT_METHOD; }

    // ================== Wallet ==================
    private static final String VERIFY_WALLET_PIN = "api/wallet/verify-pin";
    public static String getVerifyWalletPin() { return VERIFY_WALLET_PIN; }

    // ================== Orders ==================
    private static final String ORDER_REQUEST = "api/farmer/order-request";
    private static final String ALL_INCOMING_ORDERS = "api/farmer/all-incomming-orders";
    private static final String ALL_CONSUMER_ORDERS = "api/consumer/all-orders";
    private static final String ORDER_DETAIL = "api/farmer/order-detail";

    public static String getOrderRequest() { return ORDER_REQUEST; }
    public static String getAllIncomingOrders() { return ALL_INCOMING_ORDERS; }
    public static String getAllConsumerOrders() { return ALL_CONSUMER_ORDERS; }
    public static String getOrderDetail() { return ORDER_DETAIL; }

    // ================== Rating - Farmer ==================
    private static final String RATE_FARMER_CREATE = "api/rating/farmer/create";
    private static final String RATE_FARMER_EDIT = "api/rating/farmer/edit";
    private static final String RATE_FARMER_VIEW = "api/rating/farmer/view";
    private static final String RATE_FARMER_DELETE = "api/rating/farmer/delete";
    private static final String RATE_FARMER_LIST = "api/rating/farmer/list";
    private static final String FARMER_PROFILE_RATING = "api/rating/farmer/profile";

    public static String getRateFarmerCreate() { return RATE_FARMER_CREATE; }
    public static String getRateFarmerEdit() { return RATE_FARMER_EDIT; }
    public static String getRateFarmerView() { return RATE_FARMER_VIEW; }
    public static String getRateFarmerDelete() { return RATE_FARMER_DELETE; }
    public static String getRateFarmerList() { return RATE_FARMER_LIST; }
    public static String getFarmerProfileRating() { return FARMER_PROFILE_RATING; }

    // ================== Rating - Consumer ==================
    private static final String RATE_CONSUMER_CREATE = "api/rating/consumer/create";
    private static final String RATE_CONSUMER_EDIT = "api/rating/consumer/edit";
    private static final String RATE_CONSUMER_VIEW = "api/rating/consumer/view";
    private static final String RATE_CONSUMER_DELETE = "api/rating/consumer/delete";
    private static final String RATE_CONSUMER_LIST = "api/rating/consumer/list";
    private static final String CONSUMER_PROFILE_RATING = "api/rating/consumer/profile";

    public static String getRateConsumerCreate() { return RATE_CONSUMER_CREATE; }
    public static String getRateConsumerEdit() { return RATE_CONSUMER_EDIT; }
    public static String getRateConsumerView() { return RATE_CONSUMER_VIEW; }
    public static String getRateConsumerDelete() { return RATE_CONSUMER_DELETE; }
    public static String getRateConsumerList() { return RATE_CONSUMER_LIST; }
    public static String getConsumerProfileRating() { return CONSUMER_PROFILE_RATING; }

    // ================== Rating - Farmer as Consumer ==================
    private static final String RATE_FARMER_AS_CONSUMER_CREATE = "api/rating/farmer-as-consumer/create";
    private static final String RATE_FARMER_AS_CONSUMER_EDIT = "api/rating/farmer-as-consumer/edit";
    private static final String RATE_FARMER_AS_CONSUMER_DELETE = "api/rating/farmer-as-consumer/delete";

    public static String getRateFarmerAsConsumerCreate() { return RATE_FARMER_AS_CONSUMER_CREATE; }
    public static String getRateFarmerAsConsumerEdit() { return RATE_FARMER_AS_CONSUMER_EDIT; }
    public static String getRateFarmerAsConsumerDelete() { return RATE_FARMER_AS_CONSUMER_DELETE; }

    // ================== Rating - Product ==================
    private static final String RATE_PRODUCT_CREATE = "api/rating/product/create";
    private static final String RATE_PRODUCT_EDIT = "api/rating/product/edit";
    private static final String RATE_PRODUCT_VIEW = "api/rating/product/view";
    private static final String RATE_PRODUCT_DELETE = "api/rating/product/delete";
    private static final String PRODUCT_PROFILE_RATING = "api/rating/product/profile";

    public static String getRateProductCreate() { return RATE_PRODUCT_CREATE; }
    public static String getRateProductEdit() { return RATE_PRODUCT_EDIT; }
    public static String getRateProductView() { return RATE_PRODUCT_VIEW; }
    public static String getRateProductDelete() { return RATE_PRODUCT_DELETE; }
    public static String getProductProfileRating() { return PRODUCT_PROFILE_RATING; }

    // ================== Rating - Misc ==================
    private static final String TOP_RATED_FARMERS = "api/rating/top-rated-farmers";
    public static String getTopRatedFarmers() { return TOP_RATED_FARMERS; }

    // ================== Home ==================
    private static final String DASHBOARD = "api/home/dashboard";
    private static final String DASHBOARD_TEST = "api/home/dashboard-test";

    public static String getDashboardurl() { return DASHBOARD; }
    public static String getDashboardTest() { return DASHBOARD_TEST; }

    // ================== Profile ==================
    private static final String CHANGE_PASSWORD = "api/profile/change-password";
    public static String getChangePassword() { return CHANGE_PASSWORD; }

    // ================== Product ==================
    private static final String ADD_PRODUCT = "api/product/add";
    public static String getAddProduct() { return ADD_PRODUCT; }
}