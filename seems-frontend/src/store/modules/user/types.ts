export type RoleType = '' | '*' | 'admin' | 'user' | 'operator';
export type Status = '' | '正常' | '待审核' | '禁用' | '待加入';
export interface UserState {
  username?: string;
  avatar?: string;
  job?: string;
  organization?: string;
  location?: string;
  email?: string;
  introduction?: string;
  personalWebsite?: string;
  jobName?: string;
  organizationName?: string;
  locationName?: string;
  phone?: string;
  registrationDate?: string;
  accountId?: string;
  certification?: number;
  role: RoleType;
  status: Status;
}
