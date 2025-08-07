import jwt from "jsonwebtoken";

export const verifyAccessToken = async (token?: string): Promise<boolean> => {
    if (!token) return false;
    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET!);
        return !!decoded;
    } catch (err) {
        return false;
    }
};