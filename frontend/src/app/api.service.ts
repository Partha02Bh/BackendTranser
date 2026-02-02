import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private baseUrl = 'http://localhost:8080/api';  // api visible at backend for extending the repository and making the changes in the datbase.

  constructor(private http: HttpClient) { }

  login (u: string, p: string){
    // const f = new FormData();
    // f.append('username',u);
    // f.append('password',p);
    const body = {username: u, password: p};
    return this.http.post(`${this.baseUrl}/login`,body,{responseType:'text'});
  }

  verify(u:string, o:string){
    // const f = new FormData();
    // f.append('username',u);
    // f.append('opt',o);
    const body = {username: u, otp: o};
    return this.http.post<any>(`${this.baseUrl}/verify`,body);
  }

  getAccount(userId: number){
    return this.http.get<any>(`${this.baseUrl}/accounts/${userId}`);
  }

  getHistory(accId: number){
    return this.http.get<any>(`${this.baseUrl}/history/${accId}`);
  }

  getAllTransactions(){
    return this.http.get<any[]>(`${this.baseUrl}/owner/all-transactions`);
  }

  transfer(siD: number, tId: number, amt: number){
    const key = Math.random().toString(36).substring(7);
    return this.http.post(`${this.baseUrl}/transfer`,{
      sourceId: siD,
      targetId: tId,
      amount: amt,
      key: key
    })
  }

}
