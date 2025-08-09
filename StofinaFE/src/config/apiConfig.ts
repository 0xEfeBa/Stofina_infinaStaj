export const apiConfig = {
  baseUrl: process.env.BASE_URL || "http://localhost:8080/api/v1",

  auth: {
    login: "/auth/login",
  },

  customer: {
    individual: "/individual",
    corporate: "/corporate",
  },
};
