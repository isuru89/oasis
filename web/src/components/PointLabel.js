import React, { Component } from 'react'
import styled from 'styled-components';

const Wrapper = styled.div`
  font-size: 24px;
  text-align: center;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: baseline;
  user-select: none;
`

const Value = styled.span`
  font-size: ${props => props.fontSize ? props.fontSize + 'px' : '32px' };
  padding: 0 5px;
  letter-spacing: 1.4px;
  color: ${props => props.color ? props.color : 'inherit'};
  opacity: 0.9;
`

const Delta = styled.div`
  font-size: 10px;
  letter-spacing: 1px;
  font-weight: 300;
  color: ${props => props.plus ? 'hsl(100, 100%, 70%)': 'hsl(10, 100%, 70%)'};
`

const AnnotationLabel = styled.div`
  font-size: 9px;
  line-height: 2;
  color: hsl(203, 50%, 76%);
`

export default class PointLabel extends Component {
  render() {
    const { delta, value, annotation } = this.props;

    return (
      <Wrapper>
        <Value {...this.props}>{value}</Value> 
        <div>
          <Delta plus={delta > 0}>{ delta > 0 ? '+' + delta : delta }</Delta>
          <AnnotationLabel>{annotation}</AnnotationLabel>
        </div>
        
      </Wrapper>
    )
  }
}
