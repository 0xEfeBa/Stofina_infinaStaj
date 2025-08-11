export const apiConfig = {
  baseUrl: process.env.BASE_URL || "http://localhost:8080/api/v1",
  baseUrlPortfolio: process.env.BASE_URL_PORTFOLIO || "http://localhost:9001/api/v1",
  baseUrlUser: process.env.BASE_URL_USER || "http://localhost:9002/api/v1",
  baseUrlCustomer: process.env.BASE_URL_CUSTOMER || "http://localhost:9003/api/v1",
  baseUrlMail: process.env.BASE_URL_MAIL || "http://localhost:9004/api/v1",
  baseUrlMarket: process.env.BASE_URL_MARKET || "http://localhost:9005/api/v1",
  baseUrlOrder: process.env.BASE_URL_ORDER || "http://localhost:9006/api/v1",



  auth: {
    login: "/auth/login",
  },

  customer: {
    individual: "/individual",
    corporate: "/corporate",

  },

  account: {
    index: "/accounts",
    customer: "/accounts/customer/",  
  },
  stock: {
 
    index: "/stocks"
  },
  balance: {
    index: "/balances/account"
  },
  order: {
    index: "/orders"
  }
};
