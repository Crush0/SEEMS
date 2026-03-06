import axios from 'axios';

export interface ShipInfo {
  id: string;
  name: string;
  type: string;
  model: string;
  ownerCompany: string;
  manufacturer: string;
  shipOriginNumber: string;
  mmsi: string;
  imoNumber: string;
  length: number;
  lbpLength: number;
  designLwl: number;
  moldedWidth: number;
  width: number;
  moldedDepth: number;
  designDraft: number;
  maxDraft: number;
  noLoadDraft: number;
  maxLoadDraft: number;
  progress: string | number;
  towingForce: string | number;
  maxBatteryCapacity: string | number;
  sailedDistance: number;
}

/**
 * Create a new ship
 * @param shipData
 * @returns
 */
export function createNewShip(shipData: ShipInfo) {
  return axios.post('/api/ship/create', shipData);
}

export function updateShipInfo(shipData: ShipInfo) {
  return axios.post('/api/ship/update', shipData);
}

/**
 * Apply for a ship
 * @param shipNumber
 * @returns
 */
export function apply2Ship(shipNumber: string) {
  return axios.post('/api/ship/apply', { shipNumber });
}
