import axios from "axios";
const api=axios.create({baseURL:"/api",headers:{"Content-Type":"application/json"}});
api.interceptors.request.use(c=>{const t=localStorage.getItem("keystone_token");if(t)c.headers.Authorization=`Bearer ${t}`;return c;});
export async function login(email,password){const {data}=await api.post("/auth/login",{email,password});localStorage.setItem("keystone_token",data.token);localStorage.setItem("keystone_user",JSON.stringify(data));return data}
export function logout(){localStorage.removeItem("keystone_token");localStorage.removeItem("keystone_user")}
export function currentUser(){try{return JSON.parse(localStorage.getItem("keystone_user")||"null")}catch{return null}}
export default api;