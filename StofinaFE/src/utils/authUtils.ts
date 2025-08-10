import { authService } from "@/services/authService";

export const checkTokenValidity = async (): Promise<boolean> => {
  const token = authService.getAccessToken();
  return !!token;
};
